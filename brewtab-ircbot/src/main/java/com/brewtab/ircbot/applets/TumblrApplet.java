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
import java.net.URLEncoder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;

public class TumblrApplet implements BotApplet {
    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        Document doc;
        String url;

        if (unparsed.trim().length() == 0) {
            channel.write("Missing blog name");
            return;
        }

        try {
            String encodedSearchTerm = URLEncoder.encode(unparsed, "UTF8");
            Connection con;

            url = String.format("http://%s.tumblr.com/random", encodedSearchTerm);

            con = Jsoup.connect(url);
            con.execute();
            url = con.response().header("Location");
            doc = con.response().parse();

        } catch (IOException e) {
            channel.write("Error");
            return;
        }

        Element div = doc.select("div.post-content").first();
        Element title = div.getElementsByTag("a").first();
        channel.write(title.text());
        Element content = div.getElementsByTag("pre").first();
        channel.writeMultiple(content.text().replaceAll("\t", "").split("\n"));
    }
}
