package com.brewtab.ircbot.applets;

import com.brewtab.irc.IRCChannel;
import com.brewtab.irc.IRCUser;

public interface BotApplet {
    public void run(IRCChannel channel, IRCUser from, String arg0, String[] args, String unparsed);
}
