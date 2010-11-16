package org.transtruct.cmthunes.irc;

import org.transtruct.cmthunes.irc.messages.*;

/**
 * Implementing class are capable of being registered with other objects to
 * receive upstream messages
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
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
