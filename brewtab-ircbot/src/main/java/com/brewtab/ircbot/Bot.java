package com.brewtab.ircbot;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.ConnectionStateListener;
import com.brewtab.irc.client.Channel;
import com.brewtab.irc.client.Client;
import com.brewtab.irc.client.ClientFactory;
import com.brewtab.ircbot.applets.BashApplet;
import com.brewtab.ircbot.applets.CalcApplet;
import com.brewtab.ircbot.applets.EightBallApplet;
import com.brewtab.ircbot.applets.GoogleSuggestsApplet;
import com.brewtab.ircbot.applets.GroupHugApplet;
import com.brewtab.ircbot.applets.SpellApplet;
import com.brewtab.ircbot.applets.StatsApplet;
import com.brewtab.ircbot.applets.TextsFromLastNightApplet;
import com.brewtab.ircbot.applets.TumblrApplet;
import com.brewtab.ircbot.applets.UrbanDictionaryApplet;
import com.brewtab.ircbot.applets.WeatherApplet;
import com.brewtab.ircbot.applets.WikiApplet;
import com.brewtab.ircbot.applets.WolframAlphaApplet;
import com.brewtab.ircbot.util.SQLProperties;
import com.brewtab.irclog.IRCLogger;

public class Bot {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] args) throws Exception {
        InetSocketAddress addr = new InetSocketAddress("irc.brewtab.com", 6667);

        /* Create IRC client */
        Client client = ClientFactory.newClient(addr);

        /* Create logger */
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:brewtab", "sa", "");
        IRCLogger logger = new IRCLogger(connection);

        /* Create channel listener */
        BotChannelListener botChannelListener = new BotChannelListener();

        /* Simple key-value store for persistent settings/properties */
        SQLProperties properties = new SQLProperties(connection);

        /* Register applets with the bot */
        botChannelListener.registerApplet(new GroupHugApplet(), "gh", "grouphug");
        botChannelListener.registerApplet(new TextsFromLastNightApplet(), "tfln", "texts");
        botChannelListener.registerApplet(new CalcApplet(), "m", "math", "calc");
        botChannelListener.registerApplet(new WeatherApplet(properties), "w", "weather");
        botChannelListener.registerApplet(new StatsApplet(logger), "last", "bored", "tired");
        botChannelListener.registerApplet(new BashApplet(), "bash");
        botChannelListener.registerApplet(new WikiApplet(), "wiki");
        botChannelListener.registerApplet(new TumblrApplet(), "tumblr");
        botChannelListener.registerApplet(new WolframAlphaApplet("XXXX"), "a", "alpha");
        botChannelListener.registerApplet(new SpellApplet(), "sp", "spell");
        botChannelListener.registerApplet(new EightBallApplet(), "8ball");
        botChannelListener.registerApplet(new UrbanDictionaryApplet(), "urban");
        botChannelListener.registerApplet(new GoogleSuggestsApplet(), "gs");

        /* Listener for ++ and -- */
        PlusPlus plusPlus = new PlusPlus(properties);

        /* Will block until connection process is complete */
        client.setNick("testbot");
        client.setUsername("bot");
        client.setHostname("kitimat");
        client.setRealName("Mr. Bot");
        client.connect();

        /*
         * Join a channel. Channels can also be directly instantiated and
         * separately joined
         */
        Channel c = client.join("#bot");

        StringBuilder sb = new StringBuilder();
        sb.append("Hello ");
        for (String name : c.getNames()) {
            sb.append(name);
            sb.append(" ");
        }
        c.write(sb.toString());

        /* We add a handler for channel messages */
        c.addListener(botChannelListener);
        c.addListener(plusPlus);
        c.addListener(logger);

        /* Wait for client object's connection to exit and close */
        final CountDownLatch closed = new CountDownLatch(1);

        client.getConnection().addConnectionStateListener(new ConnectionStateListener() {
            @Override
            public void onConnectionConnected() {
                // --
            }

            @Override
            public void onConnectionClosing() {
                // --
            }

            @Override
            public void onConnectionClosed() {
                closed.countDown();
            }
        });

        closed.await();
        log.info("exiting");
    }
}
