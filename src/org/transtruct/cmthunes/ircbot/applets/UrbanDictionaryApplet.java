package org.transtruct.cmthunes.ircbot.applets;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import org.transtruct.cmthunes.irc.*;
import org.transtruct.cmthunes.util.URLBuilder;

public class UrbanDictionaryApplet implements BotApplet {
    private String textWithBreaks(Element element) {
        StringBuilder buffer = new StringBuilder();

        for(Node node : element.childNodes()) {
            if(node instanceof TextNode) {
                buffer.append(((TextNode) node).text().replace("\n", ""));
            } else if(node instanceof Element) {
                if(((Element) node).tagName().equals("br")) {
                    buffer.append("\n");
                } else {
                    buffer.append(this.textWithBreaks(((Element) node)));
                }
            }
        }

        return buffer.toString();
    }

    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        Document doc;
        URLBuilder url;
        int definitionIndex = 0;
        
        if(unparsed.trim().length() == 0) {
            channel.write("Missing search term");
            return;
        }
        
        if(args.length >= 2 && args[0].startsWith("-")) {
            String s = args[0].substring(1);
            try {
                definitionIndex = Integer.valueOf(s) - 1;
                unparsed = unparsed.replaceFirst(Pattern.quote(args[0]), "");
            } catch(NumberFormatException e) {
                // -
            }
        }

        try {
            Connection con;

            url = new URLBuilder("http://www.urbandictionary.com/define.php");
            url.setParameter("term", unparsed);

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
                    channel.write("Error loading page");
                    return;
                }
            }
        } catch (IOException e) {
            channel.write("Unknown error occured");
            e.printStackTrace();
            return;
        }

        if(!doc.select("div#not_defined_yet").isEmpty()) {
            channel.write("No definitions found");
        } else {
            try {
                Elements elements = doc.select("div.definition");

                if(elements.size() > definitionIndex) {
                    Element def = elements.get(definitionIndex);
                    String definition = this.textWithBreaks(def);
                    String[] lines = definition.split("\n");
                    String longestLine = lines[0];
                    String response;
                    
                    for(int i = 1; i < lines.length && longestLine.length() < 15; i++) {
                        if(lines[i].length() > longestLine.length()) {
                            longestLine = lines[i];
                        }
                    }

                    response = unparsed + ": " + longestLine;
                    channel.writeMultiple(BotAppletUtil.blockFormat(response, 400, 10));
                } else {
                    channel.write("Can not get definition");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
