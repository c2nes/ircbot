package com.brewtab.ircbot.applets;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.brewtab.irc.IRCChannel;
import com.brewtab.irc.IRCUser;

public class GroupHugApplet implements BotApplet {
    private String url;
    private LinkedBlockingQueue<String> confessions;
    private SynchronousQueue<String> errorMessage;
    private Thread confessionFetcher;
    private List<Integer> page_numbers;

    private class GetConfessions implements Runnable {
        @Override
        public void run() {
            while (true) {
                int page = page_numbers.remove(0);
                page_numbers.add(page);
                url = String.format("http://archive.grouphug.us/frontpage?page=%d", page);

                try {
                    GroupHugApplet.this.populateConfessions();
                } catch (InterruptedException e) {
                    // TODO: Add logging
                    return;
                }
            }
        }
    }

    public GroupHugApplet() {
        Random r = new Random();

        this.page_numbers = new ArrayList<Integer>();
        this.page_numbers.add(0);

        for (int i = 1; i <= 200; i++) {
            this.page_numbers.add(r.nextInt(this.page_numbers.size()), i);
        }

        this.confessions = new LinkedBlockingQueue<String>(10);
        this.errorMessage = new SynchronousQueue<String>();

        this.confessionFetcher = new Thread(new GetConfessions());
        this.confessionFetcher.setDaemon(true);
        this.confessionFetcher.start();
    }

    private void populateConfessions() throws InterruptedException {
        Connection connection;
        Connection.Response response;
        Document doc;
        String confessionId;
        String confession;
        String blurb;

        try {
            connection = Jsoup.connect(this.url);
            connection.userAgent("Wget/1.13.4 (linux-gnu)");
            connection.timeout(5000);
            connection.ignoreHttpErrors(true);
            connection.execute();

            response = connection.response();
            if (response.statusCode() != 200) {
                this.errorMessage.put(response.statusCode() + ": " + response.statusMessage());
                return;
            }

            doc = response.parse();
        } catch (SocketTimeoutException e) {
            this.errorMessage.put("Grouphug request timed out. Waiting before retrying");
            return;
        } catch (IOException e) {
            this.errorMessage.put("Caught exception while connecting to grouphug.us: " + e.getClass() + e.getMessage());
            e.printStackTrace();
            return;
        }

        for (Element element : doc.select("div.node")) {
            try {
                confessionId = element.select("a").get(0).text().trim();
                confession = element.select("p").get(0).text().trim();
                blurb = confessionId + ": " + confession;

                /* No one really wants to read a confession longer than this */
                if (blurb.length() < 800) {
                    try {
                        this.confessions.put(blurb);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                /* Failed to find needed elements */
                this.errorMessage.put("Parser error");
                return;
            }
        }
    }

    public String getConfession() {
        String error = this.errorMessage.poll();

        if (error != null) {
            return error;

        } else if (this.confessionFetcher.isAlive()) {
            try {
                return this.confessions.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        } else {
            return "Fetching thread died";
        }
    }

    @Override
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        String confession = this.getConfession();
        String[] parts = BotAppletUtil.blockFormat(confession, 300, 10);
        channel.writeMultiple(parts);
    }
}
