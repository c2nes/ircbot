package org.transtruct.cmthunes.irc.messages.filter;

/**
 * Filter for IRCMessage prefixes
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public interface IRCPrefixFilter {
    /**
     * Perform a check on the given prefix
     * 
     * @param prefix
     *            The prefix to check
     * @return true if the prefix matches, false otherwise
     */
    public boolean check(String prefix);
}
