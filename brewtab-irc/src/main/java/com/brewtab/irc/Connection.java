package com.brewtab.irc;

import java.util.List;

import org.jboss.netty.channel.ChannelFuture;

import com.brewtab.irc.messages.Message;
import com.brewtab.irc.messages.MessageListener;
import com.brewtab.irc.messages.filter.MessageFilter;

/**
 * Represents a general IRC connection. The connection may be client to server,
 * server to client, or server to server.
 */
public interface Connection {
    /**
     * Add a message listener to the connection. If this listener has had
     * already been registered with a different filter, its filter will be
     * replaced with the one provided.
     * 
     * @param filter
     * @param listener
     */
    public void addMessageListener(MessageFilter filter, MessageListener listener);

    /**
     * Remove a previously added message listener.
     * 
     * @param listener
     */
    public void removeMessageListener(MessageListener listener);

    /**
     * Add a connection state listener.
     * 
     * @param listener
     */
    public void addConnectionStateListener(ConnectionStateListener listener);

    /**
     * Remove a connection state listener.
     * 
     * @param listener
     */
    public void removeConnectionStateListener(ConnectionStateListener listener);

    /**
     * Send a message on this connection.
     * 
     * @param message
     * @return A Netty {@link ChannelFuture} object for the send operation
     */
    public ChannelFuture send(Message message);

    /**
     * Send a multiple messages on this connection.
     * 
     * @param message
     * @return A Netty {@link ChannelFuture} object for the send operation
     */
    public ChannelFuture send(Message... messages);

    /**
     * Send a message and return a response.
     * 
     * @param match a filter to match messages which should be included in the response
     * @param last a filter to match the last message of the response
     * @param message the message to send
     * @return a list of response messages matching the provided filters
     * @throws InterruptedException
     */
    public List<Message> request(MessageFilter match, MessageFilter last, Message message) throws InterruptedException;

    /**
     * Send a message and return a response.
     * 
     * @param match a filter to match messages which should be included in the response
     * @param last a filter to match the last message of the response
     * @param message the message to send
     * @return a list of response messages matching the provided filters
     * @throws InterruptedException
     */
    public List<Message> request(MessageFilter match, MessageFilter last, Message... messages) throws InterruptedException;

    /**
     * Close this connection.
     * 
     * @return A Netty {@link ChannelFuture} for the close operation
     */
    public ChannelFuture close();

    /**
     * Check if the connection is connected
     * 
     * @return
     */
    public boolean isConnected();
}
