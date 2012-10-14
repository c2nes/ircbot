package com.brewtab.ircbot.applets;

import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.brewtab.irc.IRCChannel;
import com.brewtab.irc.IRCUser;
import com.brewtab.ircbot.util.URLBuilder;

public class GoogleSuggestsApplet implements BotApplet {
    private DocumentBuilder documentBuilder;
    private XPath xpath;
    private int counter;

    public GoogleSuggestsApplet() {
        try {
            this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        this.xpath = XPathFactory.newInstance().newXPath();
        this.counter = 0;
    }

    @Override
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        URLBuilder url;
        String query = unparsed;

        if (query.trim().length() == 0) {
            channel.write("Missing query");
            return;
        }

        try {
            url = new URLBuilder("http://google.com/complete/search");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        url.setParameter("output", "toolbar");
        url.setParameter("q", query);

        try {
            Document doc = this.documentBuilder.parse(url.toString());
            NodeList suggestions = (NodeList) this.xpath.evaluate("//CompleteSuggestion", doc, XPathConstants.NODESET);

            if (suggestions.getLength() > 0) {
                int i = this.counter % suggestions.getLength();
                String suggestion = this.xpath.evaluate(
                        String.format("//CompleteSuggestion[%d]/suggestion/@data", i + 1), doc);

                channel.write(suggestion);
                this.counter++;
            } else {
                channel.write("No suggestions found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
