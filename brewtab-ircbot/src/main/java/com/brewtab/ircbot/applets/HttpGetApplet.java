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
