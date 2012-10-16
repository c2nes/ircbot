package com.brewtab.ircbot;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.brewtab.irc.IRCChannel;
import com.brewtab.irc.IRCChannelListener;
import com.brewtab.irc.IRCUser;
import com.brewtab.ircbot.util.SQLProperties;

/**
 * ChannelListener that keeps track of points for names
 * 
 * @author Chris Thunes <cthunes@brewtab.com>
 */
public class PlusPlus implements IRCChannelListener {
    private SQLProperties properties;
    private Pattern plusPlusPattern;
    private Pattern minusMinusPattern;

    public PlusPlus(SQLProperties properties) {
        this.properties = properties;

        plusPlusPattern = Pattern.compile("(\\w+)\\+\\+");
        minusMinusPattern = Pattern.compile("(\\w+)--");
    }

    @Override
    public void onJoin(IRCChannel channel, IRCUser user) {
        // --
    }

    @Override
    public void onPart(IRCChannel channel, IRCUser user) {
        // --
    }

    @Override
    public void onQuit(IRCChannel channel, IRCUser user) {
        // --
    }

    private long increment(IRCChannel channel, String key) {
        final String fullKey = "plusplus:" + channel.getName() + ":" + key;
        Long score = properties.<Long> get(fullKey);

        if (score == null) {
            score = 0L;
        }

        score++;

        properties.set(fullKey, score);
        return score;
    }

    private long decrement(IRCChannel channel, String key) {
        final String fullKey = "plusplus:" + channel.getName() + ":" + key;
        Long score = properties.<Long> get(fullKey);

        if (score == null) {
            score = 0L;
        }

        score--;

        properties.set(fullKey, score);
        return score;
    }

    @Override
    public void onPrivateMessage(IRCChannel channel, String message, IRCUser from) {
        Map<String, Long> newValues = new LinkedHashMap<String, Long>();

        Matcher matcher = plusPlusPattern.matcher(message);
        while (matcher.find()) {
            String key = matcher.group(1);
            newValues.put(key, increment(channel, key));
        }

        matcher = minusMinusPattern.matcher(message);
        while (matcher.find()) {
            String key = matcher.group(1);
            newValues.put(key, decrement(channel, key));
        }

        if (newValues.size() > 0) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;

            for (Map.Entry<String, Long> entry : newValues.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }

                sb.append(entry.getKey());
                sb.append(": ");
                sb.append(entry.getValue());
            }

            channel.write(sb.toString());
        }
    }
}
