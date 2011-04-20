package org.transtruct.cmthunes.ircbot;

import java.io.*;
import java.net.*;

import org.htmlparser.*;
import org.htmlparser.lexer.*;
import org.htmlparser.util.*;
import org.transtruct.cmthunes.util.*;

public class GroupHug {
    private URL url;
    private FixedBlockingBuffer<String> confessions;
    private String errorMessage;
    private Flag error;
    private Thread confessionFetcher;

    private class GetConfessions implements Runnable {
        @Override
        public void run() {
            while(true) {
                /* Wait for error flag to be cleared */
                GroupHug.this.error.waitUninterruptiblyFor(false);
                GroupHug.this.populateConfessions();
            }
        }
    }

    public GroupHug() {
        try {
            this.url = new URL("http://grouphug.us/random");
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }

        this.confessions = new FixedBlockingBuffer<String>(10);
        this.errorMessage = null;
        this.error = new Flag();

        this.confessionFetcher = new Thread(new GetConfessions());
        this.confessionFetcher.setDaemon(true);
        this.confessionFetcher.start();
    }

    private void populateConfessions() {
        HttpURLConnection connection;
        Lexer lexer;

        try {
            connection = (HttpURLConnection) this.url.openConnection();
            lexer = new Lexer(connection);
        } catch(IOException e) {
            this.errorMessage = "Could not establish a connection to grouphug.us";
            this.error.set();
            return;

        } catch(ParserException e) {
            this.errorMessage = "Parser error";
            this.error.set();
            return;

        }

        Node node = null;
        int state = 0;
        String confessionId = "";
        while(true) {
            try {
                node = lexer.nextNode();
                if(node == null) {
                    break;
                }

                if(node instanceof Tag) {
                    Tag tag = (Tag) node;
                    String tagId = tag.getAttribute("id");
                    String tagName = tag.getRawTagName();
                    String tagClass = tag.getAttribute("class");

                    switch(state) {
                    case 0:
                        if(tagId != null && tagId.startsWith("node-")) {
                            state = 1;
                        }
                        break;

                    case 1:
                        if(tagName != null && tagName.equals("a")) {
                            node = lexer.nextNode();
                            if(node != null && node instanceof Text) {
                                confessionId = ((Text) node).getText();
                            }
                        } else if((tagName != null && tagClass != null)
                                && (tagName.equals("div") && tagClass.equals("content"))) {
                            StringBuffer confessionBuffer = new StringBuffer();
                            while(true) {
                                node = lexer.nextNode();
                                if(node == null) {
                                    break;
                                }

                                if(node instanceof Tag) {
                                    tagName = ((Tag) node).getRawTagName();
                                    if(tagName.equals("/div")) {
                                        break;
                                    }

                                    if(tagName.equals("/p")) {
                                        confessionBuffer.append("\n");
                                    }
                                }

                                if(node instanceof Text) {
                                    String confessionPart = ((Text) node).getText();
                                    confessionPart = Translate.decode(confessionPart);
                                    confessionPart = confessionPart.replaceAll("’", "'");
                                    confessionBuffer.append(confessionPart.trim());
                                }
                            }

                            if(confessionBuffer.length() > 0) {
                                String confession = String.format("%s: %s", confessionId,
                                        confessionBuffer.toString());
                                this.confessions.add(confession.trim());
                            }
                            state = 0;
                        }
                        break;
                    }
                }

            } catch(ParserException e) {
                this.errorMessage = "Parser error";
                this.error.set();
                return;
            }
        }
    }

    public String getConfession() {
        if(this.error.isSet()) {
            String msg = this.errorMessage;
            this.error.clear();
            return msg;
        } else if(this.confessionFetcher.isAlive()) {
            return this.confessions.get();
        } else {
            return "Fetching thread died";
        }
    }
}
