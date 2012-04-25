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
        boolean foundResult = false;
        
        /* Search for a valid result at the following paths in this order */
        String[] resultSearchPaths = {
                "//pod[@primary]/subpod/plaintext",
                "//pod[2]/subpod/plaintext",
                "//pod[2]/subpod/img/@src"
        };
        

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
        url.setParameter("reinterpret", "true");
        url.setParameter("input", query);
            
        try {
            Document doc = this.documentBuilder.parse(url.toString());
            String success = this.xpath.evaluate("/queryresult/@success", doc);

            if(success.equals("true")) {
                for(String path : resultSearchPaths) {
                    String result = this.xpath.evaluate(path, doc);
                
                    if(!result.equals("")) {
                        channel.writeMultiple(result.split("\n"));
                        foundResult = true;
                        break;
                    }
                }

                if(foundResult == false) {
                    channel.write("Could not get results");
                }
            } else {
                channel.write("Could not interpret query");
            }
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
