package com.brewtab.ircbot.applets;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.Translate;

import com.brewtab.irc.IRCChannel;
import com.brewtab.irc.IRCUser;
import com.brewtab.util.Flag;

public class TextsFromLastNightApplet implements BotApplet {
    private URL url;
    private LinkedBlockingQueue<String> texts;
    private String errorMessage;
    private Flag error;
    private Thread textFetcher;

    private class GetTexts implements Runnable {
        @Override
        public void run() {
            while (true) {
                /* Wait for error flag to be cleared */
                TextsFromLastNightApplet.this.error.waitUninterruptiblyFor(false);
                TextsFromLastNightApplet.this.populateTexts();
            }
        }
    }

    public TextsFromLastNightApplet(String urlString) {
        try {
            this.url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        this.texts = new LinkedBlockingQueue<String>(10);
        this.errorMessage = null;
        this.error = new Flag();

        this.textFetcher = new Thread(new GetTexts());
        this.textFetcher.setDaemon(true);
        this.textFetcher.start();
    }

    public TextsFromLastNightApplet() {
        this("http://www.textsfromlastnight.com/Random-Texts-From-Last-Night.html");
    }

    private void populateTexts() {
        HttpURLConnection connection;
        Lexer lexer;

        try {
            connection = (HttpURLConnection) this.url.openConnection();
            lexer = new Lexer(connection);
        } catch (IOException e) {
            this.errorMessage = "Could not establish a connection to textsfromlastnight.com";
            this.error.set();
            return;

        } catch (ParserException e) {
            this.errorMessage = "Parser error";
            this.error.set();
            return;

        }

        Node node = null;

        while (true) {
            try {
                node = lexer.nextNode();
                if (node == null) {
                    break;
                }

                if (node instanceof Tag) {
                    Tag tag = (Tag) node;
                    String tagReadonly = tag.getAttribute("readonly");
                    String tagName = tag.getRawTagName();

                    if (tagName.equals("textarea") && tagReadonly != null && tagReadonly.equals("readonly")) {
                        node = lexer.nextNode();
                        if (node instanceof Text) {
                            String text = ((Text) node).getText();
                            text = Translate.decode(text);
                            try {
                                this.texts.put(text);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    }
                }

            } catch (ParserException e) {
                this.errorMessage = "Parser error";
                this.error.set();
                return;
            }
        }
    }

    public String getText() {
        if (this.error.isSet()) {
            String msg = this.errorMessage;
            this.error.clear();
            return msg;
        } else if (this.textFetcher.isAlive()) {
            try {
                return this.texts.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "error: thread interrupted";
            }
        } else {
            return "Fetching thread died";
        }
    }

    @Override
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        String text = this.getText();
        String[] parts = BotAppletUtil.blockFormat(text, 300, 10);
        channel.writeMultiple(parts);
    }
}
