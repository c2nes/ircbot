package com.brewtab.irc.client;

import java.util.List;

public interface Channel {

    /**
     * Get the channel name
     * 
     * @return the channel name
     */
    public String getName();

    /**
     * Get the associated IRCClient
     * 
     * @return the associated IRCClient
     */
    public Client getClient();

    /**
     * Part the channel
     * 
     * @param reason The reason sent with the PART message
     */
    public void part(String reason);

    /**
     * Write a message to the channel
     * 
     * @param text The message to send
     */
    public void write(String text);

    /**
     * Write multiple messages efficiently
     * 
     * @param strings The messages to send
     */
    public void writeMultiple(String... strings);

    /**
     * Retrieve the list of users in the channel. This call returns a cached
     * copy. To request a refresh of the listing from the server call
     * IRChannel#refreshNames.
     * 
     * @return the list of nicks of users in the channel
     */
    public List<String> getNames();

    /**
     * Add an IRCChannelListener to this channel.
     * 
     * @param listener the listener to add
     */
    public void addListener(ChannelListener listener);

}
