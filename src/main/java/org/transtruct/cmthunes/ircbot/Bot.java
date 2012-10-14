package org.transtruct.cmthunes.ircbot;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;

import org.transtruct.cmthunes.irc.IRCChannel;
import org.transtruct.cmthunes.irc.IRCClient;
import org.transtruct.cmthunes.irc.IRCMessageHandler;
import org.transtruct.cmthunes.irc.messages.IRCMessage;
import org.transtruct.cmthunes.irc.messages.filter.IRCMessageFilters;
import org.transtruct.cmthunes.ircbot.applets.BashApplet;
import org.transtruct.cmthunes.ircbot.applets.CalcApplet;
import org.transtruct.cmthunes.ircbot.applets.EightBallApplet;
import org.transtruct.cmthunes.ircbot.applets.GoogleSuggestsApplet;
import org.transtruct.cmthunes.ircbot.applets.GroupHugApplet;
import org.transtruct.cmthunes.ircbot.applets.SpellApplet;
import org.transtruct.cmthunes.ircbot.applets.StatsApplet;
import org.transtruct.cmthunes.ircbot.applets.TextsFromLastNightApplet;
import org.transtruct.cmthunes.ircbot.applets.TumblrApplet;
import org.transtruct.cmthunes.ircbot.applets.UrbanDictionaryApplet;
import org.transtruct.cmthunes.ircbot.applets.WeatherApplet;
import org.transtruct.cmthunes.ircbot.applets.WikiApplet;
import org.transtruct.cmthunes.ircbot.applets.WolframAlphaApplet;
import org.transtruct.cmthunes.irclog.IRCLogger;

public class Bot {
    public static void main(String[] args) throws Exception {
        InetSocketAddress addr = new InetSocketAddress("irc.brewtab.com", 6667);

        /* Create IRC client */
        IRCClient client = new IRCClient(addr);

        /* Create logger */
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:brewtab", "sa", "");
        IRCLogger logger = new IRCLogger(connection);

        /* Register applets with the bot */
        BotChannelListener botChannelListener = new BotChannelListener();
        GroupHugApplet groupHugApplet = new GroupHugApplet();
        TextsFromLastNightApplet textsFromLastNightApplet = new TextsFromLastNightApplet();
        CalcApplet calcApplet = new CalcApplet();
        WeatherApplet weatherApplet = new WeatherApplet();
        StatsApplet statsApplet = new StatsApplet(logger);
        BashApplet bashApplet = new BashApplet();
        WikiApplet wikiApplet = new WikiApplet();
        TumblrApplet tumblrApplet = new TumblrApplet();
        WolframAlphaApplet wolframAlphaApplet = new WolframAlphaApplet("XXXX");
        SpellApplet spellApplet = new SpellApplet();
        EightBallApplet eightBallApplet = new EightBallApplet();
        UrbanDictionaryApplet urbanDictionaryApplet = new UrbanDictionaryApplet();
        GoogleSuggestsApplet googleSuggestsApplet = new GoogleSuggestsApplet();

        botChannelListener.registerApplet("gh", groupHugApplet);
        botChannelListener.registerApplet("grouphug", groupHugApplet);

        botChannelListener.registerApplet("tfln", textsFromLastNightApplet);
        botChannelListener.registerApplet("texts", textsFromLastNightApplet);

        botChannelListener.registerApplet("m", calcApplet);
        botChannelListener.registerApplet("math", calcApplet);
        botChannelListener.registerApplet("calc", calcApplet);

        botChannelListener.registerApplet("w", weatherApplet);
        botChannelListener.registerApplet("weather", weatherApplet);

        botChannelListener.registerApplet("last", statsApplet);
        botChannelListener.registerApplet("bored", statsApplet);
        botChannelListener.registerApplet("tired", statsApplet);

        botChannelListener.registerApplet("bash", bashApplet);

        botChannelListener.registerApplet("wiki", wikiApplet);

        botChannelListener.registerApplet("tumblr", tumblrApplet);

        botChannelListener.registerApplet("alpha", wolframAlphaApplet);
        botChannelListener.registerApplet("a", wolframAlphaApplet);

        botChannelListener.registerApplet("spell", spellApplet);
        botChannelListener.registerApplet("sp", spellApplet);

        botChannelListener.registerApplet("8ball", eightBallApplet);

        botChannelListener.registerApplet("urban", urbanDictionaryApplet);

        botChannelListener.registerApplet("gs", googleSuggestsApplet);

        /* Will block until connection process is complete */
        client.connect("bot", "bot", "kitimat", "Mr. Bot");

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
        c.addListener(logger);

        /* Wait for client object's connection to exit and close */
        client.getConnection().awaitClosed();
        System.out.println("Exiting");
    }
}
