package org.transtruct.cmthunes.irc.messages.filter;

import org.transtruct.cmthunes.irc.messages.*;

public class IRCMessageSimpleFilter extends IRCMessageFilter {
    private IRCMessageType type;
    private IRCPrefixFilter prefixFilter;
    private String[] args;

    public IRCMessageSimpleFilter(IRCMessageType type, IRCPrefixFilter prefixFilter, String... args) {
        this.type = type;
        this.prefixFilter = prefixFilter;
        this.args = args;
    }

    public boolean check(IRCMessage message) {
        /* Check message type */
        if(this.type != null && this.type != message.getType()) {
            return false;
        }

        /* Check prefix */
        if(this.prefixFilter != null && this.prefixFilter.check(message.getPrefix()) == false) {
            return false;
        }

        /* Check arguments */
        if(this.args != null) {
            String[] msgArgs = message.getArgs();
            if(this.args.length > msgArgs.length) {
                return false;
            }

            for(int i = 0; i < this.args.length; i++) {
                if(this.args[i] != null && this.args[i].equals(msgArgs[i]) == false) {
                    return false;
                }
            }
        }

        return true;
    }
}
