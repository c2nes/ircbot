package org.transtruct.cmthunes.irc;

import org.transtruct.cmthunes.irc.messages.IRCMessage;
import org.transtruct.cmthunes.irc.protocol.IRCChannelHandler;

/**
 * A class implementing this interface can be used to manage a IRC connection
 * through an IRCChannelHandler object. Typically, a class implementing this
 * interface will be passed to a IRCChannelPipelineFactory as either an instance
 * or through an IRCConnectionManagerFactroy to actually manage a connection.
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
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
