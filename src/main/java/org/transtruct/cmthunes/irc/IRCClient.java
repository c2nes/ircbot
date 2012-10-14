package org.transtruct.cmthunes.irc;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import org.transtruct.cmthunes.irc.messages.IRCMessage;
import org.transtruct.cmthunes.irc.messages.IRCMessageType;
import org.transtruct.cmthunes.irc.messages.filter.IRCMessageFilter;
import org.transtruct.cmthunes.irc.protocol.IRCChannelHandler;
import org.transtruct.cmthunes.irc.protocol.IRCChannelPipelineFactory;
import org.transtruct.cmthunes.util.Flag;

public class IRCClient implements IRCConnectionManager {
    /* Netty objects */
    private ChannelFactory channelFactory;
    private ClientBootstrap bootstrap;
    private IRCChannelHandler channelHandler;
    private IRCChannelPipelineFactory pipelineFactory;

    /* The address of the server */
    private InetSocketAddress address;

    /* Cached copy of the server's name */
    private String servername;

    /* Connection information */
    private String pass;
    private String nick;
    private String username;
    private String hostname;
    private String realname;

    /* User object, constructed from nick, username, and hostname */
    private IRCUser user;

    /* Designate connection status */
    private Flag connected;
    private boolean connectedSuccessfully;

    /* Thread pool from which message handlers are dispatched */
    private ExecutorService threadPool;

    /* Filter -> Handler mapping */
    private HashMap<IRCMessageFilter, IRCMessageHandler> handlers;
    private HashSet<IRCMessageFilter> handlersKeys;

    /**
     * Wraps a message handler so it can be passed to the thread pool
     * 
     * @author Christopher Thunes <cthunes@transtruct.org>
     */
    private class MessageHandlerRunnable implements Runnable {
        private IRCMessageHandler handler;
        private IRCMessage message;

        /**
         * Create a new wrapper for the given handler and the given message
         * 
         * @param handler
         *            The handler to call
         * @param message
         *            The message to pass
         */
        public MessageHandlerRunnable(IRCMessageHandler handler, IRCMessage message) {
            this.handler = handler;
            this.message = message;
        }

        @Override
        public void run() {
            this.handler.handleMessage(this.message);
        }
    }

    /**
     * Construct a new IRCClient connected to the given address
     * 
     * @param address
     *            The address to connect to
     */
    public IRCClient(InetSocketAddress address) {
        this.channelFactory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        this.bootstrap = new ClientBootstrap(this.channelFactory);
        this.channelHandler = null;
        this.pipelineFactory = new IRCChannelPipelineFactory(this);

        this.bootstrap.setPipelineFactory(this.pipelineFactory);
        this.bootstrap.setOption("tcpNoDelay", true);

        this.address = address;
        this.servername = this.address.getHostName();

        this.connected = new Flag();
        this.connectedSuccessfully = false;

        this.handlers = new HashMap<IRCMessageFilter, IRCMessageHandler>();
        this.handlersKeys = new HashSet<IRCMessageFilter>();

        this.threadPool = Executors.newCachedThreadPool();

        this.pass = null;
        this.nick = null;
        this.username = null;
        this.hostname = null;
        this.realname = null;

        this.user = null;
    }

    /**
     * Get the user information give by the IRCClient#connect method as an
     * IRCUser object
     * 
     * @return the IRCUser object or null if the client is not connected yet
     */
    public IRCUser getUser() {
        return this.user;
    }

    /**
     * Connect without a password and with the given information
     * 
     * @param nick
     *            The nick to connection with
     * @param username
     *            The user name to connect with
     * @param hostname
     *            The host name to connect with
     * @param realname
     *            The real name to connect with
     * @return true if the connection was successfully made, false otherwise
     */
    public boolean connect(String nick, String username, String hostname, String realname) {
        return this.connect(null, nick, username, hostname, realname);
    }

    /**
     * Connect with a password and with the given information
     * 
     * @param pass
     *            The password to authenticate with
     * @param nick
     *            The nick to connection with
     * @param username
     *            The user name to connect with
     * @param hostname
     *            The host name to connect with
     * @param realname
     *            The real name to connect with
     * @return true if the connection was successfully made, false otherwise
     */
    public boolean connect(String pass, String nick, String username, String hostname, String realname) {
        /* Save connection information */
        this.pass = pass;
        this.nick = nick;
        this.username = username;
        this.hostname = hostname;
        this.realname = realname;

        this.user = new IRCUser(nick, username, hostname);

        /* Perform connection */
        ChannelFuture future = this.bootstrap.connect(this.address);

        /* Wait for underly socket connection to be made */
        future.awaitUninterruptibly();
        if (!future.isSuccess()) {
            /* Could not connection to remote host */
            return false;
        }

        /* Now wait for IRC connection */
        this.connected.waitUninterruptiblyFor(true);
        return this.connectedSuccessfully;
    }

    /**
     * Quit from the server and close all network connections
     * 
     * @param reason
     *            The reason for quitting as given in the QUIT message
     */
    public void quit(String reason) {
        IRCMessage quit = new IRCMessage(IRCMessageType.QUIT, reason);
        ChannelFuture future = this.channelHandler.sendMessage(quit);

        /* Wait for message to be sent and then close the underlying connection */
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                IRCClient.this.channelHandler.closeChannel();
            }
        });

    }

    /**
     * Join the given channel. Create a new channel object for this connection,
     * join the channel and return the IRCChannel object.
     * 
     * @param channelName
     *            The channel to join
     * @return the new IRCChannel object or null if the channel could not be
     *         joined
     */
    public IRCChannel join(String channelName) {
        IRCChannel channel = new IRCChannel(this, channelName);

        /* Attempt to join */
        if (channel.doJoin()) {
            return channel;
        }

        /* Error while joining */
        return null;
    }

    @Override
    public void onConnect(IRCChannelHandler connection) {
        this.channelHandler = connection;

        IRCMessage nickMessage = new IRCMessage(IRCMessageType.NICK, this.nick);
        IRCMessage userMessage = new IRCMessage(IRCMessageType.USER, this.username, this.hostname, this.servername,
                this.realname);

        if (this.pass != null) {
            /* Send a PASS message first */
            IRCMessage passMessage = null;
            if (this.pass != null) {
                passMessage = new IRCMessage(IRCMessageType.PASS, this.pass);
            }

            connection.sendMessages(passMessage, nickMessage, userMessage);
        } else {
            connection.sendMessages(nickMessage, userMessage);
        }
    }

    /**
     * Add a message handler for this connection. Messages matching the given
     * filter are passed onto the given handler
     * 
     * @param filter
     *            The filter to match messages with
     * @param handler
     *            The handler to call with matching message
     */
    public void addHandler(IRCMessageFilter filter, IRCMessageHandler handler) {
        synchronized (this.handlers) {
            this.handlers.put(filter, handler);
            this.handlersKeys.add(filter);
        }
    }

    /**
     * Remove the handler associated with the given filter
     * 
     * @param filter
     *            The filter to remove
     * @return true if successful, false otherwise
     */
    public boolean removeHandler(IRCMessageFilter filter) {
        synchronized (this.handlers) {
            if (this.handlers.containsKey(filter)) {
                this.handlers.remove(filter);
                this.handlersKeys.remove(filter);
                return true;
            }

            return false;
        }
    }

    /**
     * Return the IRCChannelHandler for this client connection
     * 
     * @return the IRCChannelHandler for this client connection
     */
    public IRCChannelHandler getConnection() {
        return this.channelHandler;
    }

    @Override
    public void onClose(IRCChannelHandler connection) {
        /*
         * With the underly connection closed we can safely shutdown the thread
         * pool
         */
        System.out.println("IRCClient.onClose");
        synchronized (this.handlers) {
            this.threadPool.shutdown();
        }
    }

    @Override
    public void onShutdown() {
        System.out.println("IRCClient.onShutdown");
        this.bootstrap.releaseExternalResources();
    }

    @Override
    public void receiveMessage(IRCChannelHandler connection, IRCMessage message) {
        switch (message.getType()) {

        /*
         * At least one of these should be received once a good connection is
         * established
         */
        case RPL_ENDOFMOTD:
        case RPL_LUSERME:
        case RPL_LUSERCHANNELS:
        case RPL_LUSERCLIENT:
        case RPL_LUSEROP:
        case RPL_LUSERUNKNOWN:
            synchronized (this.connected) {
                if (this.connected.isSet() == false) {
                    this.connected.set();
                    this.connectedSuccessfully = true;
                }
            }
            break;

        /* Error while connecting */
        case ERR_NICKNAMEINUSE:
            synchronized (this.connected) {
                if (this.connected.isSet() == false) {
                    this.connected.set();
                    this.connectedSuccessfully = false;
                }
            }
            break;

        case PING:
            IRCMessage pong = new IRCMessage(IRCMessageType.PONG, this.servername);
            connection.sendMessage(pong);
            break;

        default:
            break;
        }

        synchronized (this.handlers) {
            if (this.threadPool.isShutdown()) {
                return;
            }

            for (IRCMessageFilter filter : this.handlersKeys) {
                if (filter.check(message)) {
                    IRCMessageHandler handler = this.handlers.get(filter);
                    MessageHandlerRunnable runnableHandler = new MessageHandlerRunnable(handler, message);
                    this.threadPool.submit(runnableHandler);
                }
            }
        }
    }
}
