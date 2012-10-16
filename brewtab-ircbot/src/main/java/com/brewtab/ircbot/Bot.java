package com.brewtab.ircbot;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;

import com.brewtab.irc.IRCChannel;
import com.brewtab.irc.IRCClient;
import com.brewtab.irc.IRCMessageHandler;
import com.brewtab.irc.messages.IRCMessage;
import com.brewtab.irc.messages.filter.IRCMessageFilters;
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
    public static void main(String[] args) throws Exception {
        InetSocketAddress addr = new InetSocketAddress("irc.brewtab.com", 6667);

        /* Create IRC client */
        IRCClient client = new IRCClient(addr);

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
        client.connect("testbot", "bot", "kitimat", "Mr. Bot");

        /*
         * We can add a message handler for the client to print all messages
         * from the server if we needed to for debugging
         */
        client.addHandler(IRCMessageFilters.PASS, new IRCMessageHandler() {
            @Override
            public void handleMessage(IRCMessage message) {
                System.out.println("root>>> " + message.toString().trim());
            }
        });

        /*
         * Join a channel. Channels can also be directly instantiated and
         * separately joined
         */
        IRCChannel c = client.join("#bot");

        /* We add a handler for channel messages */
        c.addListener(botChannelListener);
        c.addListener(plusPlus);
        c.addListener(logger);

        /* Wait for client object's connection to exit and close */
        client.getConnection().awaitClosed();
        System.out.println("Exiting");
    }
}
