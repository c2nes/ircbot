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
