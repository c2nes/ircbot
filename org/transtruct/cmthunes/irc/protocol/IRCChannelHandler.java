package org.transtruct.cmthunes.irc.protocol;

import org.transtruct.cmthunes.irc.*;
import org.transtruct.cmthunes.irc.messages.*;
import org.transtruct.cmthunes.util.*;

import org.jboss.netty.channel.*;

public class IRCChannelHandler extends SimpleChannelHandler implements IRCMessagePipe {
    private IRCConnectionManager connectionManager;

    /* Flag set once closed */
    private Flag connectionClosed;
    private Flag connectionClosing;

    /* Channel is stored after connect */
    private Channel channel;

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

    public void closeChannel() throws IRCNotConnectedException {
        synchronized(this.connectionClosing) {
            /* Connection already has started being closed */
            if(this.connectionClosing.isSet()) {
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
            } catch(NullPointerException e) {
                throw new IRCNotConnectedException("Not connected");
            }
        }
    }

    public void awaitClosed() {
        this.connectionClosed.waitUninterruptiblyFor(true);
        this.connectionManager.onShutdown();
    }

    @Override
    public ChannelFuture sendMessage(IRCMessage message) throws IRCNotConnectedException {
        try {
            return this.channel.write(message);
        } catch(NullPointerException e) {
            throw new IRCNotConnectedException("Not yet connected");
        }
    }

    @Override
    public ChannelFuture sendMessages(IRCMessage... messages) throws IRCNotConnectedException {
        try {
            return this.channel.write(messages);
        } catch(NullPointerException e) {
            throw new IRCNotConnectedException("Not yet connected");
        }
    }
}
