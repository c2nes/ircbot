package com.brewtab.irc.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.brewtab.irc.IRCConnectionManager;
import com.brewtab.irc.messages.IRCMessage;
import com.brewtab.util.Flag;

/**
 * An IRCChannelHandler interfaces a high-level IRCConnectionManager to the
 * lower level Netty pipeline. An IRCChannelHandler passes requests upstream to
 * a IRCConnectionManager and allows IRCMessage objects to be sent downstream to
 * the Netty pipeline.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public class IRCChannelHandler extends SimpleChannelHandler implements IRCMessagePipe {
    /** The connection manager which requests are passed to */
    private IRCConnectionManager connectionManager;

    /** Set once the connection has be closed */
    private Flag connectionClosed;

    /** Set once the process of closing the connection has started */
    private Flag connectionClosing;

    /**
     * Once the connection is made, the underly channel is stored so that
     * messages can be sent out asynchronously
     */
    private Channel channel;

    /**
     * Initialize a new IRCChannelHandler using the given IRCConnectionManager
     * 
     * @param connectionManager
     *            The high level manager for the connection
     */
    public IRCChannelHandler(IRCConnectionManager connectionManager) {
        super();

        this.connectionManager = connectionManager;
        this.channel = null;

        this.connectionClosed = new Flag();
        this.connectionClosing = new Flag();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        IRCMessage message = (IRCMessage) e.getMessage();
        this.connectionManager.receiveMessage(this, message);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        /* Store channel */
        this.channel = ctx.getChannel();
        this.connectionManager.onConnect(this);
    }

    @Override
    public void closeRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        this.connectionManager.onClose(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        System.err.println("Exception caught: " + e.getCause().getMessage());
        e.getCause().printStackTrace();
        ctx.getChannel().close();
    }

    /**
     * Close the connection. The closing process is performed asynchronously, so
     * IRChannelHandler#awaitClosed() should be called to wait for this process
     * to complete
     * 
     * @throws IRCNotConnectedException
     *             if the connection is not connected
     */
    public void closeChannel() throws IRCNotConnectedException {
        synchronized (this.connectionClosing) {
            /* Connection already has started being closed */
            if (this.connectionClosing.isSet()) {
                return;
            }

            /* Attempt to close channel */
            try {
                ChannelFuture future = this.channel.close();
                this.connectionClosing.set();

                future.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        IRCChannelHandler.this.connectionClosed.set();
                    }
                });
            } catch (NullPointerException e) {
                throw new IRCNotConnectedException("Not connected");
            }
        }
    }

    /**
     * Wait for the connection to complete close and for the connection manager
     * to shut down
     */
    public void awaitClosed() {
        this.connectionClosed.waitUninterruptiblyFor(true);
        this.connectionManager.onShutdown();
    }

    /**
     * Send a message through this connection
     */
    @Override
    public ChannelFuture sendMessage(IRCMessage message) throws IRCNotConnectedException {
        try {
            return this.channel.write(message);
        } catch (NullPointerException e) {
            throw new IRCNotConnectedException("Not yet connected");
        }
    }

    /**
     * Send multiple messages through this connection
     */
    @Override
    public ChannelFuture sendMessages(IRCMessage... messages) throws IRCNotConnectedException {
        try {
            return this.channel.write(messages);
        } catch (NullPointerException e) {
            throw new IRCNotConnectedException("Not yet connected");
        }
    }
}
