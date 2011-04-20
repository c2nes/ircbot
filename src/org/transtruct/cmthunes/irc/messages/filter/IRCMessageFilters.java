package org.transtruct.cmthunes.irc.messages.filter;

import org.transtruct.cmthunes.irc.messages.*;

/**
 * Provides convenient filters
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public class IRCMessageFilters {
    /** A filter that matches any message */
    public static final IRCMessageFilter PASS = new IRCMessageFilter() {
        @Override
        public boolean check(IRCMessage message) {
            return true;
        }
    };

    /**
     * Construct and return a filter which will match a message pertaining to
     * the given channel
     * 
     * @param channelName
     *            The channel to construct a filter for
     * @return the new filter
     */
    public static IRCMessageFilter newChannelFilter(String channelName) {
        IRCMessageFilter privMsg, join, part, names, endOfNames, topic, noTopic, filter;

        /* Individual possible messages */
        privMsg = new IRCMessageSimpleFilter(IRCMessageType.PRIVMSG, null, channelName);
        join = new IRCMessageSimpleFilter(IRCMessageType.JOIN, null, channelName);
        part = new IRCMessageSimpleFilter(IRCMessageType.PART, null, channelName);
        names = new IRCMessageSimpleFilter(IRCMessageType.RPL_NAMREPLY, null, null, "=", channelName);
        endOfNames = new IRCMessageSimpleFilter(IRCMessageType.RPL_ENDOFNAMES, null, null, channelName);
        topic = new IRCMessageSimpleFilter(IRCMessageType.RPL_TOPIC, null, channelName);
        noTopic = new IRCMessageSimpleFilter(IRCMessageType.RPL_NOTOPIC, null, channelName);

        /* Combined filter */
        filter = new IRCMessageOrFilter(privMsg, join, part, names, endOfNames, topic, noTopic);

        return filter;
    }

    /**
     * Construct a filter which will match any message of the given type
     * 
     * @param type
     *            The type to match
     * @return the new filter
     */
    public static IRCMessageFilter newTypeFilter(IRCMessageType type) {
        return new IRCMessageSimpleFilter(type, null);
    }
}
