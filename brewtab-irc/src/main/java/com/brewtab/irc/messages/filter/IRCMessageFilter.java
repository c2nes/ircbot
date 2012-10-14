package com.brewtab.irc.messages.filter;

import com.brewtab.irc.messages.IRCMessage;

/**
 * An IRCMessageFilter is responsible for performing matching operations on
 * IRCMessages.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public interface IRCMessageFilter {
    /**
     * Check if the given message is accepted by the filter
     * 
     * @param message
     *            The message to check
     * @return true if the messages matches, false otherwise
     */
    public boolean check(IRCMessage message);
}
