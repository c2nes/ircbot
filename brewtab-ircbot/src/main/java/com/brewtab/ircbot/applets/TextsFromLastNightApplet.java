/*
 * Copyright (c) 2013 Christopher Thunes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.brewtab.ircbot.applets;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.Translate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;

public class TextsFromLastNightApplet implements BotApplet {
    private static final Logger log = LoggerFactory.getLogger(TextsFromLastNightApplet.class);

    private URL url;
    private LinkedBlockingQueue<String> texts;
    private SynchronousQueue<String> errorMessage;
    private Thread textFetcher;

    private class GetTexts implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    TextsFromLastNightApplet.this.populateTexts();
                } catch (InterruptedException e) {
                    log.warn("populator thread interrupted, exiting");
                    return;
                }
            }
        }
    }

    public TextsFromLastNightApplet(String urlString) {
        try {
            this.url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        this.texts = new LinkedBlockingQueue<String>(10);
        this.errorMessage = new SynchronousQueue<String>();

        this.textFetcher = new Thread(new GetTexts());
        this.textFetcher.setDaemon(true);
        this.textFetcher.start();
    }

    public TextsFromLastNightApplet() {
        this("http://www.textsfromlastnight.com/Random-Texts-From-Last-Night.html");
    }

    private void populateTexts() throws InterruptedException {
        HttpURLConnection connection;
        Lexer lexer;

        try {
            connection = (HttpURLConnection) this.url.openConnection();
            lexer = new Lexer(connection);
        } catch (IOException e) {
            log.warn("could not open connection", e);
            this.errorMessage.put("Could not establish a connection to textsfromlastnight.com");
            return;

        } catch (ParserException e) {
            log.error("parse error", e);
            this.errorMessage.put("Parser error");
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
                            this.texts.put(text);
                        }
                    }
                }

            } catch (ParserException e) {
                log.error("parse error", e);
                this.errorMessage.put("Parser error");
                return;
            }
        }
    }

    public String getText() {
        String error = this.errorMessage.poll();

        if (error != null) {
            return error;

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
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        String text = this.getText();
        String[] parts = BotAppletUtil.blockFormat(text, 300, 10);
        channel.writeMultiple(parts);
    }
}
