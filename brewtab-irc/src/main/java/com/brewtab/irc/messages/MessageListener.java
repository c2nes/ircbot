package com.brewtab.irc.messages;


/**
 * Implementing class are capable of being registered with other objects to
 * receive upstream messages
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public interface MessageListener {
    /**
     * Handle a message
     * 
     * @param message
     *            The message to process
     */
    public void onMessage(Message message);
}
