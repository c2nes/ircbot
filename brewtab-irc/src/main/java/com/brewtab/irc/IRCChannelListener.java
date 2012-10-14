package com.brewtab.irc;

/**
 * Implementing class can listen for IRC channel related events
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public interface IRCChannelListener {
    /**
     * Called when a user joins the channel
     * 
     * @param channel
     *            The channel that was joined
     * @param user
     *            The user that joined
     */
    public void onJoin(IRCChannel channel, IRCUser user);

    /**
     * Called when a user parts the channel
     * 
     * @param channel
     *            The channel that was parted from
     * @param user
     *            The user that parted
     */
    public void onPart(IRCChannel channel, IRCUser user);

    /**
     * Called when a user quits the channel
     * 
     * @param channel
     *            The channel that was quit from
     * @param user
     *            The user that parted
     */
    public void onQuit(IRCChannel channel, IRCUser user);

    /**
     * Called whenever a message is sent to the channel
     * 
     * @param channel
     *            The channel the message was received on
     * @param message
     *            The message
     * @param from
     *            The user that sent the message
     */
    public void onPrivateMessage(IRCChannel channel, String message, IRCUser from);
}
