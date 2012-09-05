package org.transtruct.cmthunes.ircbot.applets;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import org.transtruct.cmthunes.irc.*;
import org.transtruct.cmthunes.util.*;

public class WikiApplet implements BotApplet {
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        Document doc;
        URLBuilder url;

        if(unparsed.trim().length() == 0) {
            channel.write("Missing article name");
            return;
        }
        
        if(unparsed.trim().equals("-r")) {
            unparsed = "Special:Random";
        }

        try {
            Connection con;

            url = new URLBuilder("http://en.wikipedia.org/w/index.php");
            url.setParameter("search", unparsed);

            while(true) {
                con = Jsoup.connect(url.toString());
                con.followRedirects(false);
                con.execute();
            
                if(con.response().statusCode() == 200) {
                    doc = con.response().parse();
                    break;
                } else if(con.response().statusCode() == 302) {
                    url = new URLBuilder(con.response().header("Location"));
                } else {
                    channel.write("Could not find page");
                    return;
                }
            }
        } catch(MalformedURLException e) {
            e.printStackTrace();
            return;
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

                    channel.write(url.toString());
                    channel.writeMultiple(BotAppletUtil.blockFormat(summary, 400, 10));
                } else {
                    channel.write("Can not get article summary");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
