package org.transtruct.cmthunes.irc.messages.filter;

import org.transtruct.cmthunes.irc.messages.*;

public class IRCMessageAndFilter extends IRCMessageFilter {
    private IRCMessageFilter[] filters;

    public IRCMessageAndFilter(IRCMessageFilter... filters) {
        this.filters = filters;
    }

    public boolean check(IRCMessage message) {
        for(IRCMessageFilter filter : filters) {
            if(filter.check(message) == false) {
                return false;
            }
        }

        return true;
    }
}
