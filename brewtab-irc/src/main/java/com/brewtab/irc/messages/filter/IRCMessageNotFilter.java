package com.brewtab.irc.messages.filter;

import com.brewtab.irc.messages.IRCMessage;

/**
 * Negate a filter
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public class IRCMessageNotFilter implements IRCMessageFilter {
    private IRCMessageFilter filter;

    /**
     * Initialize a new filter which matches exactly with the given filter does
     * not and vice versa
     * 
     * @param filter
     *            The filter to negate
     */
    public IRCMessageNotFilter(IRCMessageFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean check(IRCMessage message) {
        return (!this.filter.check(message));
    }
}