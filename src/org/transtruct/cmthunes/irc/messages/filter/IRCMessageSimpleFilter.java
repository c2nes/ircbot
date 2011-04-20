package org.transtruct.cmthunes.irc.messages.filter;

import org.transtruct.cmthunes.irc.messages.*;

/**
 * Filter for IRCMessage objects. Filters messages based on their type, prefix,
 * and arguments
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public class IRCMessageSimpleFilter extends IRCMessageFilter {
    /** Type of the message to match or null */
    private IRCMessageType type;

    /** A prefix filter to filter the prefix of the message */
    private IRCPrefixFilter prefixFilter;

    /** The argument constraints. null arguments are wildcards */
    private String[] args;

    /**
     * Initialize a new filter matching the given constraints. Specifying
     * {@code null} for an argument will match anything for that part.
     * 
     * @param type
     *            The type of the message to match or {@code null}
     * @param prefixFilter
     *            A IRCPrefixFilter to check message prefixes or {@code null}
     * @param args
     *            A list of arguments to match. The number of arguments to not
     *            have to be equal, but any message must contain at least as
     *            many arguments as provided in this constraint. The list of
     *            arguments must match the first arguments of the message. If
     *            any argument is {@code null} it will be treated as a wildcard.
     */
    public IRCMessageSimpleFilter(IRCMessageType type, IRCPrefixFilter prefixFilter, String... args) {
        this.type = type;
        this.prefixFilter = prefixFilter;
        this.args = args;
    }

    @Override
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
