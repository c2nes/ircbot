package org.transtruct.cmthunes.ircbot.applets;

import org.transtruct.cmthunes.irc.*;

public interface BotApplet {
    public void run(IRCChannel channel, IRCUser from, String arg0, String[] args, String unparsed);
}
