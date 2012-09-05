package org.transtruct.cmthunes.irc.protocol;

import org.transtruct.cmthunes.irc.messages.*;

import org.jboss.netty.channel.*;

/**
 * Implementing this interface allows an object to act as a pipe for sending IRC
 * messages
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public interface IRCMessagePipe {
    /**
     * Send the given message
     * 
     * @param message
     *            Message to send
     * @return a Netty ChannelFuture representing the send operation
     * @throws IRCNotConnectedException
     *             if the underlying stream is not connected
     * @see org.jboss.netty.channel.ChannelFuture
     */
    public ChannelFuture sendMessage(IRCMessage message) throws IRCNotConnectedException;

    /**
     * Send the given messages
     * 
     * @param messages
     *            Messages to send
     * @return a Netty ChannelFuture representing the send operation
     * @throws IRCNotConnectedException
     *             if the underlying stream is not connected
     * @see org.jboss.netty.channel.ChannelFuture
     */
    public ChannelFuture sendMessages(IRCMessage... messages) throws IRCNotConnectedException;
}
