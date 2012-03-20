package org.transtruct.cmthunes.ircbot.applets;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import org.transtruct.cmthunes.irc.*;
import org.transtruct.cmthunes.util.*;

public class WolframAlphaApplet implements BotApplet {
    private DocumentBuilder documentBuilder;
    private XPath xpath;
    private String appid;

    public WolframAlphaApplet(String appid) {
        try {
            this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch(ParserConfigurationException e) {
            e.printStackTrace();
        }

        this.xpath = XPathFactory.newInstance().newXPath();
        this.appid = appid;
    }

    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        URLBuilder url;
        String query = unparsed;

        if(query.trim().length() == 0) {
            channel.write("Missing query");
            return;
        }
        
        try {
            url = new URLBuilder("http://api.wolframalpha.com/v2/query");
        } catch(MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        url.setParameter("appid", this.appid);
        url.setParameter("format", "plaintext");
        url.setParameter("input", query);
            
        try {
            Document doc = this.documentBuilder.parse(url.toString());
            String success = this.xpath.evaluate("/queryresult/@success", doc);
            String result = this.xpath.evaluate("//pod[@primary]/subpod/plaintext", doc);

            if(success.equals("true")) {
                channel.writeMultiple(result.split("\n"));
            } else {
                channel.write("Could not interpret query");
            }
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
