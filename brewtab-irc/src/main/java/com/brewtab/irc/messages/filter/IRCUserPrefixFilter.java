package com.brewtab.irc.messages.filter;

import com.brewtab.irc.IRCUser;

/**
 * Filter for IRCMessage prefixes which specify a user/host rather than a
 * server. User prefixes are in the form nick!user@host.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public class IRCUserPrefixFilter implements IRCPrefixFilter {
    private String nick;
    private String user;
    private String host;

    /**
     * Initialize a new IRCUserPrefixFilter to match on the given constraints.
     * Specify {@code null} for a parameter to exclude it form the match
     * (wildcard).
     * 
     * @param nick
     *            The nick to match or {@code null}
     * @param user
     *            The user to match or {@code null}
     * @param host
     *            The host to match or {@code null}
     */
    public IRCUserPrefixFilter(String nick, String user, String host) {
        this.nick = nick;
        this.user = user;
        this.host = host;
    }

    /**
     * Initialize a new IRCUserPrefixFilter to match on the given constraints.
     * Specify {@code null} for a parameter to exclude it form the match
     * (wildcard).
     * 
     * @param nick
     *            The nick to match or {@code null}
     * @param user
     *            The user to match or {@code null}
     */
    public IRCUserPrefixFilter(String nick, String user) {
        this(nick, user, null);
    }

    /**
     * Initialize a new IRCUserPrefixFilter to match on the given constraints.
     * Specify {@code null} for a parameter to exclude it form the match
     * (wildcard).
     * 
     * @param nick
     *            The nick to match or {@code null}
     */
    public IRCUserPrefixFilter(String nick) {
        this(nick, null, null);
    }

    @Override
    public boolean check(String prefix) {
        IRCUser user = IRCUser.fromString(prefix);
        if (user == null) {
            return false;
        }

        if (this.nick != null && user.getNick().equals(this.nick) == false) {
            return false;
        }
        if (this.user != null && user.getUser().equals(this.user) == false) {
            return false;
        }
        if (this.host != null && user.getHost().equals(this.host) == false) {
            return false;
        }

        return true;
    }
}
