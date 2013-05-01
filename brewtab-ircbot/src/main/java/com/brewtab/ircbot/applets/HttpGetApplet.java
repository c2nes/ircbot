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

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;

public class HttpGetApplet implements BotApplet {
    private static final Logger log = LoggerFactory.getLogger(HttpGetApplet.class);

    private String url;

    public HttpGetApplet(String url) {
        this.url = url;
    }

    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        try {
            Connection con;

            con = Jsoup.connect(url.toString());
            con.followRedirects(true);
            con.execute();

            if (con.response().statusCode() == 200) {
                String[] response = BotAppletUtil.blockFormat(con.response().body(), 300, 10);
                channel.writeMultiple(response);
            } else {
                channel.write("Error retreiving page: " + con.response().statusCode());
            }
        } catch (IOException e) {
            channel.write("Unknown error occured");
            log.error("error retrieving page", e);
            return;
        }
    }
}
