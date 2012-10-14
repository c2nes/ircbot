package com.brewtab.irc;

import com.brewtab.irc.messages.IRCMessage;

/**
 * Implementing class are capable of being registered with other objects to
 * receive upstream messages
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public interface IRCMessageHandler {
    /**
     * Handle a message
     * 
     * @param message
     *            The message to process
     */
    public void handleMessage(IRCMessage message);
}
