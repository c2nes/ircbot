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

package com.brewtab.irclog;

import java.util.Date;

public class IRCLogEvent {
    public static int JOIN_EVENT = 0;
    public static int PART_EVENT = 1;
    public static int QUIT_EVENT = 2;
    public static int MESSAGE_EVENT = 3;

    private Date date;
    private int eventType;
    private String channel;
    private String nick;
    private String data;

    public IRCLogEvent(int eventType) {
        this.eventType = eventType;
    }

    public IRCLogEvent(Date date, int eventType, String channel, String nick, String data) {
        this.date = date;
        this.eventType = eventType;
        this.channel = channel;
        this.nick = nick;
        this.data = data;
    }

    public IRCLogEvent(Date date, int eventType, String channel, String nick) {
        this(date, eventType, channel, nick, null);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return this.date;
    }

    public int getEventType() {
        return this.eventType;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return this.nick;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }
}
