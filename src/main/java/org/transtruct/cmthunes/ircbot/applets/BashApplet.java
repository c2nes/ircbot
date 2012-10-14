package org.transtruct.cmthunes.ircbot.applets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.transtruct.cmthunes.irc.IRCChannel;
import org.transtruct.cmthunes.irc.IRCUser;

public class BashApplet implements BotApplet {
    private Thread quoteFetcher;
    private ArrayBlockingQueue<String[]> quotes;

    private class GetQuotes implements Runnable {
        @Override
        public void run() {
            while (true) {
                BashApplet.this.populateQuotes();
            }
        }
    }

    public BashApplet() {
        this.quotes = new ArrayBlockingQueue<String[]>(10);

        this.quoteFetcher = new Thread(new GetQuotes());
        this.quoteFetcher.setDaemon(true);
        this.quoteFetcher.start();
    }

    private void populateQuotes() {
        Document doc;
        String quoteId = null;
        String quoteScore = null;

        try {
            doc = Jsoup.connect("http://bash.org/?random1").get();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (Element element : doc.select("p.quote, p.qt")) {
            if (element.className().equals("quote")) {
                quoteId = element.select("b").first().text();
                quoteScore = element.ownText();
            } else {
                StringBuilder buffer = new StringBuilder();
                ArrayList<String> quote = new ArrayList<String>();
                String[] lines;

                for (Node node : element.childNodes()) {
                    if (node instanceof TextNode) {
                        buffer.append(((TextNode) node).text().trim());
                    } else if (node instanceof Element) {
                        if (((Element) node).tagName().equals("br")) {
                            buffer.append("\n");
                        }
                    }
                }

                lines = buffer.toString().split("\n");

                if (lines.length == 1) {
                    quote.add(String.format("%s %s: %s", quoteId, quoteScore, lines[0]));
                } else {
                    quote.add(String.format("%s %s:", quoteId, quoteScore));

                    for (String line : lines) {
                        quote.add(" " + line);
                    }
                }

                try {
                    this.quotes.put(quote.toArray(new String[0]));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        try {
            String[] quote = this.quotes.take();
            channel.writeMultiple(quote);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
