package com.brewtab.ircbot;

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
import com.brewtab.ircbot.applets.WikiApplet;
import com.brewtab.ircbot.applets.WolframAlphaApplet;
import com.brewtab.ircbot.applets.WundergroundApplet;
import com.brewtab.ircbot.util.SQLProperties;
import com.brewtab.irclog.IRCLogger;

public class Bot implements ConnectionStateListener {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    private static String WOLFRAM_ALPHA_KEY = "XXXX";
    private static String WUNDERGROUND_KEY = "XXXX";

    private String connectSpec;

    private Client client;
    private Channel channel;

    private Connection connection;
    private IRCLogger logger;
    private SQLProperties properties;

    private AppletListener appletsListener;
    private PlusPlus plusPlus;

    private CountDownLatch disconnected;

    public Bot(String connectSpec) {
        this.connectSpec = connectSpec;
    }

    private void initApplets() {
        appletsListener.registerApplet(new GroupHugApplet(), "gh", "grouphug");
        appletsListener.registerApplet(new TextsFromLastNightApplet(), "tfln", "texts");
        appletsListener.registerApplet(new CalcApplet(), "m", "math", "calc");
        appletsListener.registerApplet(new WundergroundApplet(properties, WUNDERGROUND_KEY), "w", "weather");
        appletsListener.registerApplet(new StatsApplet(logger), "last", "bored", "tired");
        appletsListener.registerApplet(new BashApplet(), "bash");
        appletsListener.registerApplet(new WikiApplet(), "wiki");
        appletsListener.registerApplet(new TumblrApplet(), "tumblr");
        appletsListener.registerApplet(new WolframAlphaApplet(WOLFRAM_ALPHA_KEY), "a", "alpha");
        appletsListener.registerApplet(new SpellApplet(), "sp", "spell");
        appletsListener.registerApplet(new EightBallApplet(), "8ball");
        appletsListener.registerApplet(new UrbanDictionaryApplet(), "urban");
        appletsListener.registerApplet(new GoogleSuggestsApplet(), "gs");
    }

    public void start() throws Exception {
        client = ClientFactory.newInstance().connect(connectSpec);

        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:brewtab", "sa", "");
        logger = new IRCLogger(connection);
        properties = new SQLProperties(connection);

        plusPlus = new PlusPlus(properties);
        appletsListener = new AppletListener();
        initApplets();

        channel = client.join("#bot");
        channel.addListener(appletsListener);
        channel.addListener(plusPlus);
        channel.addListener(logger);

        disconnected = new CountDownLatch(1);
    }

    private void awaitDisconnected() throws InterruptedException {
        disconnected.await();
    }

    @Override
    public void onConnectionClosed() {
        disconnected.countDown();
    }

    @Override
    public void onConnectionConnected() {
        // --
    }

    @Override
    public void onConnectionClosing() {
        // --
    }

    public static void main(String[] args) throws Exception {
        Bot bot = new Bot("irc://testbot@irc.brewtab.com/");

        try {
            bot.start();
        } catch (Exception e) {
            log.error("Error starting bot", e);
            return;
        }

        try {
            bot.awaitDisconnected();
        } catch (InterruptedException e) {
            log.error("main thread interrupted, exiting");
        }

        log.info("exiting");
    }
}
