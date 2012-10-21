package com.brewtab.ircbot.applets;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;

public interface BotApplet {
    public void run(Channel channel, User from, String arg0, String[] args, String unparsed);
}
