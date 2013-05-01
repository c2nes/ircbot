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

package com.brewtab.ircbot.applets;

import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.ircbot.util.URLBuilder;

public class GoogleSuggestsApplet implements BotApplet {
    private static final Logger log = LoggerFactory.getLogger(GoogleSuggestsApplet.class);

    private DocumentBuilder documentBuilder;
    private XPath xpath;
    private int counter;

    public GoogleSuggestsApplet() {
        try {
            this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        this.xpath = XPathFactory.newInstance().newXPath();
        this.counter = 0;
    }

    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        URLBuilder url;
        String query = unparsed;

        if (query.trim().length() == 0) {
            channel.write("Missing query");
            return;
        }

        try {
            url = new URLBuilder("http://google.com/complete/search");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        url.setParameter("output", "toolbar");
        url.setParameter("q", query);

        try {
            Document doc = this.documentBuilder.parse(url.toString());
            NodeList suggestions = (NodeList) this.xpath.evaluate("//CompleteSuggestion", doc, XPathConstants.NODESET);

            if (suggestions.getLength() > 0) {
                int i = this.counter % suggestions.getLength();
                String suggestion = this.xpath.evaluate(
                        String.format("//CompleteSuggestion[%d]/suggestion/@data", i + 1), doc);

                channel.write(suggestion);
                this.counter++;
            } else {
                channel.write("No suggestions found");
            }
        } catch (Exception e) {
            log.error("exception while extracting suggestion from page", e);
            return;
        }
    }
}
