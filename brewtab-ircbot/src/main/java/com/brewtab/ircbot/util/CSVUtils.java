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

package com.brewtab.ircbot.util;

import java.util.ArrayList;
import java.util.List;

public class CSVUtils {

    public static List<String> csvSplit(String csvLine) {
        ArrayList<String> fields = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int state = 0;
        char c;

        for (int i = 0; i < csvLine.length(); i++) {
            c = csvLine.charAt(i);

            switch (state) {
            case 0:
                /* Consume whitespace */
                if (Character.isWhitespace(c)) {
                    break;
                }
                state = 1;

            case 1: // Reading field
                if (c == '\"') {
                    /* Enter quoted string */
                    state = 2;
                } else if (Character.isWhitespace(c) || c == ',') {
                    /* End of field */
                    fields.add(sb.toString());
                    sb = new StringBuilder();

                    if (c == ',') {
                        state = 0;
                    } else {
                        state = 4;
                    }
                } else {
                    sb.append(c);
                }
                break;

            case 2: // Inside quotes
                if (c == '\\') {
                    state = 3;
                } else if (c == '"') {
                    state = 1;
                } else {
                    sb.append(c);
                }
                break;

            case 3: // Escape character
                if (c == '"') {
                    sb.append('"');
                }
                state = 2;
                break;

            case 4: // End of argument
                if (c == ',') {
                    state = 0;
                } else if (!Character.isWhitespace(c)) {
                    throw new IllegalArgumentException("Expected comma after field, " + csvLine);
                }
                break;
            }
        }

        if (state == 2 || state == 3) {
            throw new IllegalArgumentException("Unmatched quote, " + csvLine);
        }

        /* Store last field */
        if (sb.length() > 0) {
            fields.add(sb.toString());
        }

        return fields;
    }

}
