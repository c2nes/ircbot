/*
 * Copyright (c) 2013 Christopher Thunes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.brewtab.ircbot.applets.stock;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.ircbot.util.CSVUtils;
import com.brewtab.ircbot.util.URLBuilder;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

public class Quote {
    private static final Logger log = LoggerFactory.getLogger(Quote.class);
    private static final String baseURL = "http://finance.yahoo.com/d/quotes.csv";

    private EnumMap<QuoteField, String> values;

    public Quote(Map<QuoteField, String> values) {
        this.values = new EnumMap<QuoteField, String>(values);
    }

    public String get(QuoteField field) {
        String v = values.get(field);

        if (v == null) {
            throw new IllegalArgumentException("Field not populated");
        }

        return v;
    }

    public List<QuoteField> fields() {
        return ImmutableList.copyOf(values.keySet());
    }

    private static List<List<String>> query(List<String> symbols, List<QuoteField> fields) {
        StringBuilder sb = new StringBuilder();

        for (QuoteField field : fields) {
            sb.append(field.getTag());
        }

        String tags = sb.toString();

        try {
            URLBuilder urlBuilder = new URLBuilder(baseURL);
            urlBuilder.setParameter("s", Joiner.on(' ').join(symbols));
            urlBuilder.setParameter("f", tags);

            log.info("Stock request: {}", urlBuilder.toString());

            InputStream s = urlBuilder.toUrl().openStream();
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];

            while (true) {
                int n = s.read(buff);

                if (n < 0) {
                    break;
                }

                data.write(buff, 0, n);
            }

            s.close();

            String csvData = data.toString();
            String[] csvLines = csvData.split("\n");
            List<List<String>> results = new ArrayList<List<String>>();

            for (String csvLine : csvLines) {
                results.add(CSVUtils.csvSplit(csvLine));
            }

            return results;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static Quote forSymbol(String symbol, List<QuoteField> fields) {
        return forSymbols(Arrays.asList(symbol), fields).get(0);
    }

    public static List<Quote> forSymbols(List<String> symbols, List<QuoteField> fields) {
        List<Quote> quotes = new ArrayList<Quote>(symbols.size());
        List<List<String>> quotesValues = query(symbols, fields);

        for (List<String> values : quotesValues) {
            if (values.size() != fields.size()) {
                throw new RuntimeException("Number of fields returned does not match number of fields requested");
            }

            Map<QuoteField, String> m = new EnumMap<QuoteField, String>(QuoteField.class);

            for (int i = 0; i < fields.size(); i++) {
                m.put(fields.get(i), values.get(i));
            }

            quotes.add(new Quote(m));
        }

        return quotes;
    }
}
