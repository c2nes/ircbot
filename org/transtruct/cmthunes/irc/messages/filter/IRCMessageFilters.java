package org.transtruct.cmthunes.irc.messages.filter;

import org.transtruct.cmthunes.irc.messages.*;

public class IRCMessageFilters {
    public static final IRCMessageFilter PASS = new IRCMessageFilter() {
        @Override
        public boolean check(IRCMessage message) {
            return true;
        }
    };
    
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
    
    public static IRCMessageFilter newTypeFilter(IRCMessageType type) {
        return new IRCMessageSimpleFilter(type, null);
    }
}
