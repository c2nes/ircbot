package com.brewtab.ircbot.applets;

import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.ircbot.util.URLBuilder;

public class WolframAlphaApplet implements BotApplet {
    private static final Logger log = LoggerFactory.getLogger(WolframAlphaApplet.class);

    private DocumentBuilder documentBuilder;
    private XPath xpath;
    private String appid;

    public WolframAlphaApplet(String appid) {
        try {
            this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        this.xpath = XPathFactory.newInstance().newXPath();
        this.appid = appid;
    }

    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        URLBuilder url;
        String query = unparsed;
        boolean foundResult = false;

        /* Search for a valid result at the following paths in this order */
        String[] resultSearchPaths = { "//pod[@primary]/subpod/plaintext", "//pod[2]/subpod/plaintext",
                "//pod[2]/subpod/img/@src" };

        if (query.trim().length() == 0) {
            channel.write("Missing query");
            return;
        }

        try {
            url = new URLBuilder("http://api.wolframalpha.com/v2/query");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        url.setParameter("appid", this.appid);
        url.setParameter("reinterpret", "true");
        url.setParameter("input", query);

        try {
            Document doc = this.documentBuilder.parse(url.toString());
            String success = this.xpath.evaluate("/queryresult/@success", doc);

            if (success.equals("true")) {
                for (String path : resultSearchPaths) {
                    String result = this.xpath.evaluate(path, doc);

                    if (!result.equals("")) {
                        channel.writeMultiple(result.split("\n"));
                        foundResult = true;
                        break;
                    }
                }

                if (foundResult == false) {
                    channel.write("Could not get results");
                }
            } else {
                channel.write("Could not interpret query");
            }
        } catch (Exception e) {
            log.error("caught exception while parsing results", e);
            return;
        }
    }
}
