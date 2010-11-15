package org.transtruct.cmthunes.irc;

public class IRCUser {
    private String nick;
    private String user;
    private String host;

    public IRCUser(String nick, String user, String host) {
        this.nick = nick;
        this.user = user;
        this.host = host;
    }

    public String getNick() {
        return this.nick;
    }

    public String getUser() {
        return this.user;
    }

    public String getHost() {
        return this.host;
    }

    public static IRCUser fromString(String prefix) {
        if(prefix == null) {
            return null;
        }

        int endNick = prefix.indexOf('!');
        int endUser = prefix.indexOf('@');

        if(endNick == -1 || endUser == -1 || endUser < endNick) {
            return null;
        }

        String nick = prefix.substring(0, endNick);
        String user = prefix.substring(endNick + 1, endUser);
        String host = prefix.substring(endUser + 1);

        return new IRCUser(nick, user, host);
    }
}
