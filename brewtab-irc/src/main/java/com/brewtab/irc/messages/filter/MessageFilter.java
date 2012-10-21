package com.brewtab.irc.messages.filter;

import com.brewtab.irc.messages.Message;

/**
 * An IRCMessageFilter is responsible for performing matching operations on
 * IRCMessages.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public interface MessageFilter {
    /**
     * Check if the given message is accepted by the filter
     * 
     * @param message
     *            The message to check
     * @return true if the messages matches, false otherwise
     */
    public boolean check(Message message);
}
