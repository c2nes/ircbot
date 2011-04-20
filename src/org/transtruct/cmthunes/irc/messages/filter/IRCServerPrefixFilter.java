package org.transtruct.cmthunes.irc.messages.filter;

/**
 * Filter for IRCMessage prefixes which specify a server rather than a user/host
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public class IRCServerPrefixFilter implements IRCPrefixFilter {
    private String servername;

    /**
     * Initialize a new IRCServerPrefixFilter to match the given server name
     * 
     * @param servername
     *            The server name to match
     */
    public IRCServerPrefixFilter(String servername) {
        this.servername = servername;
    }

    @Override
    public boolean check(String prefix) {
        return this.servername.equals(prefix);
    }
}
