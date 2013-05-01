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

import java.io.IOException;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.ircbot.util.URLBuilder;

public class UrbanDictionaryApplet implements BotApplet {
    private static final Logger log = LoggerFactory.getLogger(UrbanDictionaryApplet.class);

    private String textWithBreaks(Element element) {
        StringBuilder buffer = new StringBuilder();

        for (Node node : element.childNodes()) {
            if (node instanceof TextNode) {
                buffer.append(((TextNode) node).text().replace("\n", ""));
            } else if (node instanceof Element) {
                if (((Element) node).tagName().equals("br")) {
                    buffer.append("\n");
                } else {
                    buffer.append(this.textWithBreaks(((Element) node)));
                }
            }
        }

        return buffer.toString();
    }

    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        Document doc;
        URLBuilder url;
        int definitionIndex = 0;
        boolean random = false;

        if (unparsed.trim().length() == 0) {
            random = true;
        }

        if (args.length == 1 && args[0].equals("-r")) {
            random = true;
        }

        if (args.length >= 2 && args[0].startsWith("-")) {
            String s = args[0].substring(1);
            try {
                definitionIndex = Integer.valueOf(s) - 1;
                unparsed = unparsed.replaceFirst(Pattern.quote(args[0]), "");
            } catch (NumberFormatException e) {
                // -
            }
        }

        try {
            Connection con;

            if (random) {
                url = new URLBuilder("http://www.urbandictionary.com/random.php");
            } else {
                url = new URLBuilder("http://www.urbandictionary.com/define.php");
                url.setParameter("term", unparsed);
            }

            while (true) {
                con = Jsoup.connect(url.toString());
                con.followRedirects(false);
                con.execute();

                if (con.response().statusCode() == 200) {
                    doc = con.response().parse();
                    break;
                } else if (con.response().statusCode() == 302) {
                    url = new URLBuilder(con.response().header("Location"));
                } else {
                    channel.write("Error loading page");
                    return;
                }
            }
        } catch (IOException e) {
            channel.write("Unknown error occured");
            log.error("caught exception while loading page", e);
            return;
        }

        if (!doc.select("div#not_defined_yet").isEmpty()) {
            channel.write("No definitions found");
        } else {
            try {
                Elements elements = doc.select("div.definition");

                if (elements.size() > definitionIndex) {
                    Element def = elements.get(definitionIndex);
                    String definition = this.textWithBreaks(def);
                    String[] lines = definition.split("\n");
                    String longestLine = lines[0];
                    String response;

                    for (int i = 1; i < lines.length && longestLine.length() < 15; i++) {
                        if (lines[i].length() > longestLine.length()) {
                            longestLine = lines[i];
                        }
                    }

                    response = url.getQueryParameter("term") + ": " + longestLine;
                    channel.writeMultiple(BotAppletUtil.blockFormat(response, 400, 10));
                } else {
                    channel.write("Can not get definition");
                }
            } catch (Exception e) {
                log.error("caught exception while parsing", e);
            }
        }
    }
}
