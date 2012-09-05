package org.transtruct.cmthunes.irc.messages.filter;

import org.transtruct.cmthunes.irc.messages.*;

/**
 * A filter that matches if any of its constituent parts do. That is, it acts
 * like an "or" operation on a number of other filters.
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public class IRCMessageOrFilter implements IRCMessageFilter {
    /** The contained filters */
    private IRCMessageFilter[] filters;

    /**
     * Initialize a new filter which matches if and only if at least one of the
     * given filters matches
     * 
     * @param filters
     *            A list of filters to combine
     */
    public IRCMessageOrFilter(IRCMessageFilter... filters) {
        this.filters = filters;
    }

    @Override
    public boolean check(IRCMessage message) {
        for(IRCMessageFilter filter : filters) {
            if(filter.check(message) == true) {
                return true;
            }
        }

        return false;
    }
}
