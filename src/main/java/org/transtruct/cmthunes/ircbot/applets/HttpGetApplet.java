package org.transtruct.cmthunes.ircbot.applets;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import org.transtruct.cmthunes.irc.IRCChannel;
import org.transtruct.cmthunes.irc.IRCUser;

public class HttpGetApplet implements BotApplet {
    private String url;

    public HttpGetApplet(String url) {
        this.url = url;
    }

    @Override
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
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
            e.printStackTrace();
            return;
        }
    }
}
