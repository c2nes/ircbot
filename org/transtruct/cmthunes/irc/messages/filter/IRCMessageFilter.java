package org.transtruct.cmthunes.irc.messages.filter;

import org.transtruct.cmthunes.irc.messages.*;

public abstract class IRCMessageFilter {
    public abstract boolean check(IRCMessage message);
}
