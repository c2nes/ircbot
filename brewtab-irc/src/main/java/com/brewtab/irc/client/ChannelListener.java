package com.brewtab.irc.client;

import com.brewtab.irc.User;

/**
 * Implementing class can listen for IRC channel related events
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public interface ChannelListener {
    /**
     * Called when a user joins the channel
     * 
     * @param channel The channel that was joined
     * @param user The user that joined
     */
    public void onJoin(Channel channel, User user);

    /**
     * Called when a user parts the channel
     * 
     * @param channel The channel that was parted from
     * @param user The user that parted
     */
    public void onPart(Channel channel, User user);

    /**
     * Called when a user quits the channel
     * 
     * @param channel The channel that was quit from
     * @param user The user that parted
     */
    public void onQuit(Channel channel, User user);

    /**
     * Called whenever a message is sent to the channel
     * 
     * @param channel The channel the message was received on
     * @param from The user that sent the message
     * @param message The message
     */
    public void onMessage(Channel channel, User from, String message);
}
