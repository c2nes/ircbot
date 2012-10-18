package com.brewtab.irc.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.brewtab.irc.IRCChannel;
import com.brewtab.irc.IRCClient;
import com.brewtab.irc.IRCPrivateChat;

public class Log4jAppender extends AppenderSkeleton {
    private AtomicBoolean initialized = new AtomicBoolean(false);
    private BlockingQueue<LogLine> buffer = new PriorityBlockingQueue<LogLine>();

    private IRCClient client;
    private Thread background;
    private volatile boolean running = true;

    private String serverAddress;
    private int port = 6667;
    private String nick = "log4j";
    private String localhost;
    private String quitMessage = "brewtab IRC log4j appender quiting";
    private String target;

    private static class LogLine implements Comparable<LogLine> {
        private Long timestamp;
        private String line;

        public LogLine(long timestamp, String line) {
            this.timestamp = timestamp;
            this.line = line;
        }

        public String getLine() {
            return line;
        }

        @Override
        public int compareTo(LogLine o) {
            return timestamp.compareTo(o.timestamp);
        }
    }

    private class ChatRunner implements Runnable {
        private String nick;

        public ChatRunner(String nick) {
            this.nick = nick;
        }

        @Override
        public void run() {
            initClient();

            final IRCPrivateChat chat = client.getPrivateChat(nick);

            while (running) {
                final LogLine line;

                try {
                    line = buffer.take();
                } catch (InterruptedException e) {
                    continue;
                }

                chat.write(line.getLine());
            }
        }
    }

    private class ChannelRunner implements Runnable {
        private String channelName;

        public ChannelRunner(String channelName) {
            this.channelName = channelName;
        }

        @Override
        public void run() {
            initClient();

            final IRCChannel channel = client.join(channelName);

            while (running) {
                final LogLine line;

                try {
                    line = buffer.take();
                } catch (InterruptedException e) {
                    continue;
                }

                channel.write(line.getLine());
            }

            channel.part(quitMessage);
        }
    }

    @Override
    public void close() {
        running = false;
        background.interrupt();

        try {
            background.join();
        } catch (InterruptedException e) {
            // --
        }
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    private void initClient() {
        if (localhost == null) {
            try {
                localhost = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                localhost = "localhost";
            }
        }

        client = new IRCClient(new InetSocketAddress(serverAddress, port));
        client.connect(nick, "log4j", localhost, "Brewtab IRC log4j appender");
    }

    private void init() {
        final Runnable runner;

        if (target.charAt(0) == '#') {
            runner = new ChannelRunner(target);
        } else {
            runner = new ChatRunner(target);
        }

        background = new Thread(runner);
        background.setName("log4j-irc-appender");
        background.setDaemon(true);
        background.start();
    }

    @Override
    protected void append(LoggingEvent event) {
        if (!initialized.getAndSet(true)) {
            init();
        }

        String line = getLayout().format(event);
        buffer.offer(new LogLine(event.getTimeStamp(), line));
    }

    public void setQuitMessage(String quitMessage) {
        this.quitMessage = quitMessage;
    }

    public void setServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setTarget(String target) {
        this.target = target.trim();
    }

    public void setLocalhost(String hostname) {
        this.localhost = hostname;
    }
}
