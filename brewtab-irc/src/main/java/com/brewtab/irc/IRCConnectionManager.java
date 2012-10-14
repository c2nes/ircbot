package com.brewtab.irc;

import com.brewtab.irc.messages.IRCMessage;
import com.brewtab.irc.protocol.IRCChannelHandler;

/**
 * A class implementing this interface can be used to manage a IRC connection
 * through an IRCChannelHandler object. Typically, a class implementing this
 * interface will be passed to a IRCChannelPipelineFactory as either an instance
 * or through an IRCConnectionManagerFactroy to actually manage a connection.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public interface IRCConnectionManager {
    /**
     * Called by the underlying IRChannelHandler once the connection has been
     * established
     * 
     * @param connection
     *            The associated IRCChannelHandler
     */
    public void onConnect(IRCChannelHandler connection);

    /**
     * Called by the underlying IRCChannelHandler once a close request has been
     * made on the channel
     * 
     * @param connection
     */
    public void onClose(IRCChannelHandler connection);

    /**
     * Called by the underlying IRCChannelHandler once the IRCChannelHandler is
     * shut down
     */
    public void onShutdown();

    /**
     * Called by the underlying IRCChannelHandler whenever it receive a message
     * 
     * @param connection
     *            The calling IRCChannelHandler
     * @param message
     *            The received message
     */
    public void receiveMessage(IRCChannelHandler connection, IRCMessage message);
}
