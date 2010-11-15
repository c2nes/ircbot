package org.transtruct.cmthunes.irc.messages.filter;

import org.transtruct.cmthunes.irc.messages.*;

public class IRCMessageOrFilter extends IRCMessageFilter {
    private IRCMessageFilter[] filters;

    public IRCMessageOrFilter(IRCMessageFilter... filters) {
        this.filters = filters;
    }

    public boolean check(IRCMessage message) {
        for(IRCMessageFilter filter : filters) {
            if(filter.check(message) == true) {
                return true;
            }
        }

        return false;
    }
}
