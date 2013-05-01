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

package com.brewtab.ircbot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
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
import com.brewtab.ircbot.applets.StockApplet;
import com.brewtab.ircbot.applets.TextsFromLastNightApplet;
import com.brewtab.ircbot.applets.TumblrApplet;
import com.brewtab.ircbot.applets.UrbanDictionaryApplet;
import com.brewtab.ircbot.applets.WikiApplet;
import com.brewtab.ircbot.applets.WolframAlphaApplet;
import com.brewtab.ircbot.applets.WundergroundApplet;
import com.brewtab.ircbot.util.SQLProperties;
import com.brewtab.irclog.IRCLogger;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

public class Bot implements ConnectionStateListener {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    @Argument(value = "config", description = "Configuration properties file")
    private static String configFile;

    private String connectSpec;

    private Client client;
    private Channel channel;

    private Connection connection;
    private IRCLogger logger;
    private SQLProperties properties;

    private AppletListener appletsListener;
    private PlusPlus plusPlus;

    private String channelName;

    private String wundergroundApiKey;
    private String wolframAlphaApiKey;

    private String database;
    private String databaseHost;
    private String databaseUser;
    private String databasePassword;

    private CountDownLatch disconnected;

    public Bot(String connectSpec) {
        this.connectSpec = connectSpec;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getWundergroundApiKey() {
        return wundergroundApiKey;
    }

    public void setWundergroundApiKey(String wundergroundApiKey) {
        this.wundergroundApiKey = wundergroundApiKey;
    }

    public String getWolframAlphaApiKey() {
        return wolframAlphaApiKey;
    }

    public void setWolframAlphaApiKey(String wolframAlphaApiKey) {
        this.wolframAlphaApiKey = wolframAlphaApiKey;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDatabaseHost() {
        return databaseHost;
    }

    public void setDatabaseHost(String databaseHost) {
        this.databaseHost = databaseHost;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    private void initApplets() {
        appletsListener.registerApplet(new GroupHugApplet(), "gh", "grouphug");
        appletsListener.registerApplet(new TextsFromLastNightApplet(), "tfln", "texts");
        appletsListener.registerApplet(new CalcApplet(), "m", "math", "calc");
        appletsListener.registerApplet(new WundergroundApplet(properties, wundergroundApiKey), "w", "weather");
        appletsListener.registerApplet(new StatsApplet(logger), "last", "bored", "tired", "search");
        appletsListener.registerApplet(new BashApplet(), "bash");
        appletsListener.registerApplet(new WikiApplet(), "wiki");
        appletsListener.registerApplet(new TumblrApplet(), "tumblr");
        appletsListener.registerApplet(new WolframAlphaApplet(wolframAlphaApiKey), "a", "alpha");
        appletsListener.registerApplet(new SpellApplet(), "sp", "spell");
        appletsListener.registerApplet(new EightBallApplet(), "8ball");
        appletsListener.registerApplet(new UrbanDictionaryApplet(), "urban");
        appletsListener.registerApplet(new GoogleSuggestsApplet(), "gs");
        appletsListener.registerApplet(new StockApplet(), "stock");
    }

    private Connection createConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection("jdbc:postgresql://" + databaseHost + "/" + database, databaseUser,
            databasePassword);
    }

    public void start() throws Exception {
        client = ClientFactory.newInstance().connect(connectSpec);

        connection = createConnection();
        logger = new IRCLogger(connection);
        properties = new SQLProperties(connection);

        plusPlus = new PlusPlus(properties);
        appletsListener = new AppletListener();
        initApplets();

        channel = client.join(channelName);
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
        try {
            Args.parse(Bot.class, args);
        } catch (IllegalArgumentException e) {
            Args.usage(Bot.class);
            return;
        }

        Properties config = new Properties();
        InputStream configInputStream;

        if (configFile == null) {
            configInputStream = Bot.class.getResourceAsStream("bot.properties");

            if (configInputStream == null) {
                log.error("Could not find configuration in classpath");
                return;
            }

            config.load(configInputStream);
        } else {
            try {
                configInputStream = new FileInputStream(configFile);
            } catch (FileNotFoundException e) {
                log.error("Could not open configuration file");
                return;
            }

            config.load(configInputStream);
        }

        Bot bot = new Bot(config.getProperty("connectUrl"));

        bot.setChannelName(config.getProperty("channel"));
        bot.setWolframAlphaApiKey(config.getProperty("applets.wolframAlpha.apiKey"));
        bot.setWundergroundApiKey(config.getProperty("applets.wunderground.apiKey"));
        bot.setDatabase(config.getProperty("database.db"));
        bot.setDatabaseHost(config.getProperty("database.host"));
        bot.setDatabaseUser(config.getProperty("database.user"));
        bot.setDatabasePassword(config.getProperty("database.password"));

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
