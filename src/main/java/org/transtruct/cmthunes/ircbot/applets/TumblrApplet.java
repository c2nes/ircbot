package org.transtruct.cmthunes.ircbot.applets;

import java.io.IOException;
import java.net.URLEncoder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.transtruct.cmthunes.irc.IRCChannel;
import org.transtruct.cmthunes.irc.IRCUser;

public class TumblrApplet implements BotApplet {
    @Override
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
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
