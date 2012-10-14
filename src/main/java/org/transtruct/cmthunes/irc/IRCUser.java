package org.transtruct.cmthunes.irc;

/**
 * Simple object encapsulating a user's nick, username, and hostname
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public class IRCUser {
    /** The nick */
    private String nick;

    /** The user */
    private String user;

    /** The host */
    private String host;

    /**
     * Construct a new IRCUser with the given parameters
     * 
     * @param nick
     *            The user's nick
     * @param user
     *            The user's username
     * @param host
     *            The user's hostname
     */
    public IRCUser(String nick, String user, String host) {
        this.nick = nick;
        this.user = user;
        this.host = host;
    }

    /**
     * Get the nick
     * 
     * @return the nick
     */
    public String getNick() {
        return this.nick;
    }

    /**
     * Return the user name
     * 
     * @return the user name
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Return the host name
     * 
     * @return the host name
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Construct a new IRCUser from the give String. The String should be in the
     * format {@literal <nick>!<user>@<host>}. This format is the same as is
     * used in IRC message prefixes
     * 
     * @param prefix
     *            The prefix to extract the information from
     * @return a new IRCUser object or null if the prefix could not be parsed
     */
    public static IRCUser fromString(String prefix) {
        if (prefix == null) {
            return null;
        }

        int endNick = prefix.indexOf('!');
        int endUser = prefix.indexOf('@');

        if (endNick == -1 || endUser == -1 || endUser < endNick) {
            return null;
        }

        String nick = prefix.substring(0, endNick);
        String user = prefix.substring(endNick + 1, endUser);
        String host = prefix.substring(endUser + 1);

        return new IRCUser(nick, user, host);
    }
}
