package org.transtruct.cmthunes.irc.messages.filter;

import org.transtruct.cmthunes.irc.messages.*;

public class IRCMessageNotFilter extends IRCMessageFilter {
    private IRCMessageFilter filter;

    public IRCMessageNotFilter(IRCMessageFilter filter) {
        this.filter = filter;
    }

    public boolean check(IRCMessage message) {
        return (!this.filter.check(message));
    }
}
