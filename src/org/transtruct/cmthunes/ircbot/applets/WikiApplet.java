package org.transtruct.cmthunes.ircbot.applets;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import org.transtruct.cmthunes.irc.*;

public class WikiApplet implements BotApplet {
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        Document doc;
        String url;

        if(unparsed.trim().length() == 0) {
            channel.write("Missing article name");
            return;
        }

        try {
            String encodedArticleName = URLEncoder.encode(unparsed.replace(" ", "_"), "UTF8");
            url = String.format("http://en.wikipedia.org/wiki/%s", encodedArticleName);
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            channel.write("No such article");
            return;
        }

        if(!doc.select("div.noarticletext").isEmpty()) {
            channel.write("No such article");
        } else {
            try {
                Elements elements = doc.select("div.mw-content-ltr > p");

                if(elements.size() > 0) {
                    Element p = elements.first();
                    String summary = p.text();

                    channel.write(url);
                    channel.writeMultiple(BotAppletUtil.blockFormat(summary, 400, 10));
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
