package com.brewtab.irc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple object encapsulating a user's nick, username, and hostname
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public class User {
    private static final Pattern userPrefixPattern;

    static {
        final String specialNickCharset = Pattern.quote("[]\\`_^{}|");
        final String nickFirstCharacter = "[a-zA-Z" + specialNickCharset + "]";
        final String nickRest = "[a-zA-Z0-9\\-" + specialNickCharset + "]*";

        final String nick = nickFirstCharacter + nickRest;
        final String user = "[^@]+";
        final String host = ".+";

        userPrefixPattern = Pattern.compile("^(" + nick + ")((!(" + user + "))?@(" + host + "))?$");
    }

    /** The nick */
    private String nick;

    /** The user */
    private String user;

    /** The host */
    private String host;

    /**
     * Construct a new IRCUser with the given parameters. Only a nick is
     * required, but if a user is provided a host must also be provided.
     * 
     * @param nick The user's nick
     * @param user The user's username, or null for none
     * @param host The user's hostname, or null for none
     */
    public User(String nick, String user, String host) {
        if (nick == null) {
            throw new IllegalArgumentException("nick can not be null");
        }

        if (user != null && host == null) {
            throw new IllegalArgumentException("can not provide username without hostname");
        }

        this.nick = nick;
        this.user = user;
        this.host = host;
    }

    public User(String nick) {
        this(nick, null, null);
    }

    public User(String nick, String host) {
        this(nick, null, host);
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
     * @param prefix The prefix to extract the information from
     * @return a new IRCUser object or null if the prefix could not be parsed
     */
    public static User fromPrefix(String prefix) {
        Matcher matcher = userPrefixPattern.matcher(prefix);

        if (matcher.find()) {
            String nick = matcher.group(1);
            String user = matcher.group(4);
            String host = matcher.group(5);

            return new User(nick, user, host);
        } else {
            return null;
        }
    }

    public String toPrefix() {
        StringBuilder sb = new StringBuilder();

        sb.append(nick);

        if (host != null) {
            if (user != null) {
                sb.append('!');
                sb.append(user);
            }

            sb.append('@');
            sb.append(host);
        }

        return sb.toString();
    }
}
