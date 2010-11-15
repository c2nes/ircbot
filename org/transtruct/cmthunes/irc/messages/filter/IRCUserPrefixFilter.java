package org.transtruct.cmthunes.irc.messages.filter;

import org.transtruct.cmthunes.irc.*;

public class IRCUserPrefixFilter implements IRCPrefixFilter {
    private String nick;
    private String user;
    private String host;

    public IRCUserPrefixFilter(String nick, String user, String host) {
        this.nick = nick;
        this.user = user;
        this.host = host;
    }

    public IRCUserPrefixFilter(String nick, String user) {
        this(nick, user, null);
    }

    public IRCUserPrefixFilter(String nick) {
        this(nick, null, null);
    }

    @Override
    public boolean check(String prefix) {
        IRCUser user = IRCUser.fromString(prefix);
        if(user == null) {
            return false;
        }

        if(this.nick != null && user.getNick().equals(this.nick) == false) {
            return false;
        }
        if(this.user != null && user.getUser().equals(this.user) == false) {
            return false;
        }
        if(this.host != null && user.getHost().equals(this.host) == false) {
            return false;
        }

        return true;
    }
}
