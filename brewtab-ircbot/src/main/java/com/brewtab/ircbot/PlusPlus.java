package com.brewtab.ircbot;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.irc.client.ChannelListener;
import com.brewtab.ircbot.util.SQLProperties;

/**
 * ChannelListener that keeps track of points for names
 * 
 * @author Chris Thunes <cthunes@brewtab.com>
 */
public class PlusPlus implements ChannelListener {
    private SQLProperties properties;
    private Pattern plusPlusPattern;
    private Pattern minusMinusPattern;

    public PlusPlus(SQLProperties properties) {
        this.properties = properties;

        plusPlusPattern = Pattern.compile("(\\w+)\\+\\+");
        minusMinusPattern = Pattern.compile("(\\w+)--");
    }

    @Override
    public void onJoin(Channel channel, User user) {
        // --
    }

    @Override
    public void onPart(Channel channel, User user) {
        // --
    }

    @Override
    public void onQuit(Channel channel, User user) {
        // --
    }

    private long increment(Channel channel, String key) {
        final String fullKey = "plusplus:" + channel.getName() + ":" + key;
        Long score = properties.<Long> get(fullKey);

        if (score == null) {
            score = 0L;
        }

        score++;

        properties.set(fullKey, score);
        return score;
    }

    private long decrement(Channel channel, String key) {
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
    public void onMessage(Channel channel, User from, String message) {
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
