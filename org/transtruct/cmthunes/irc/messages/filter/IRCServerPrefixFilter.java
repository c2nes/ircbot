package org.transtruct.cmthunes.irc.messages.filter;

public class IRCServerPrefixFilter implements IRCPrefixFilter {
    private String servername;

    public IRCServerPrefixFilter(String servername) {
        this.servername = servername;
    }

    @Override
    public boolean check(String prefix) {
        return this.servername.equals(prefix);
    }
}
