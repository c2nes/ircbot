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
