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
import java.net.MalformedURLException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.ircbot.util.URLBuilder;

public class WikiApplet implements BotApplet {
    private static final Logger log = LoggerFactory.getLogger(WikiApplet.class);

    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        Document doc;
        URLBuilder url;

        if (unparsed.trim().length() == 0) {
            channel.write("Missing article name");
            return;
        }

        if (unparsed.trim().equals("-r")) {
            unparsed = "Special:Random";
        }

        try {
            Connection con;

            url = new URLBuilder("http://en.wikipedia.org/w/index.php");
            url.setParameter("search", unparsed);

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
                    channel.write("Could not find page");
                    return;
                }
            }
        } catch (MalformedURLException e) {
            log.error("malformed url", e);
            return;
        } catch (IOException e) {
            log.warn("could not retrieve article", e);
            channel.write("No such article");
            return;
        }

        if (!doc.select("div.noarticletext").isEmpty()) {
            channel.write("No such article");
        } else {
            try {
                Elements elements = doc.select("div.mw-content-ltr > p");

                if (elements.size() > 0) {
                    Element p = elements.first();
                    String summary = p.text();

                    channel.write(url.toString());
                    channel.writeMultiple(BotAppletUtil.blockFormat(summary, 400, 10));
                } else {
                    channel.write("Can not get article summary");
                }
            } catch (Exception e) {
                log.error("could not parse document", e);
            }
        }
    }
}
