package org.transtruct.cmthunes.irc;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.transtruct.cmthunes.irc.messages.*;
import org.transtruct.cmthunes.irc.messages.filter.*;
import org.transtruct.cmthunes.irc.protocol.*;
import org.transtruct.cmthunes.irc.util.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.*;

public class IRCClient implements IRCConnectionManager {
    private InetSocketAddress address;
    private ChannelFactory channelFactory;
    private ClientBootstrap bootstrap;
    private IRCChannelHandler channelHandler;
    private IRCChannelPipelineFactory pipelineFactory;
    
    private String servername;

    /* Connection information */
    private String pass;
    private String nick;
    private String username;
    private String hostname;
    private String realname;

    /* User object, constructed from nick, username, and hostname */
    private IRCUser user;

    private Flag connected;
    private boolean connectedSuccessfully;

    private ExecutorService threadPool;

    private HashMap<IRCMessageFilter, IRCMessageHandler> handlers;
    private HashSet<IRCMessageFilter> handlersKeys;

    private class MessageHandlerRunnable implements Runnable {
        private IRCMessageHandler handler;
        private IRCMessage message;

        public MessageHandlerRunnable(IRCMessageHandler handler, IRCMessage message) {
            this.handler = handler;
            this.message = message;
        }

        @Override
        public void run() {
            this.handler.handleMessage(this.message);
        }
    }

    public IRCClient(InetSocketAddress address) {
        this.channelFactory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        this.bootstrap = new ClientBootstrap(this.channelFactory);
        this.pipelineFactory = new IRCChannelPipelineFactory(this);
        
        this.bootstrap.setPipelineFactory(this.pipelineFactory);
        this.bootstrap.setOption("tcpNoDelay", true);
        
        this.address = address;
        this.servername = this.address.getHostName();

        this.channelHandler = null;
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

    public IRCUser getUser() {
        return this.user;
    }

    public boolean connect(String nick, String username, String host, String realname) {
        return this.connect(null, nick, username, host, realname);
    }

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
        if(!future.isSuccess()) {
            /* Could not connection to remote host */
            return false;
        }
        
        /* Now wait for IRC connection */
        this.connected.waitUninterruptiblyFor(true);
        return this.connectedSuccessfully;
    }

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

    public IRCChannel join(String channelName) {
        IRCChannel channel = new IRCChannel(this, channelName);

        /* Attempt to join */
        if(channel.doJoin()) {
            return channel;
        }

        /* Error while joining */
        return null;
    }

    @Override
    public void onConnect(IRCChannelHandler connection) {
        this.channelHandler = connection;

        IRCMessage nickMessage = new IRCMessage(IRCMessageType.NICK, this.nick);
        IRCMessage userMessage = new IRCMessage(IRCMessageType.USER, this.username, this.hostname,
                this.servername, this.realname);

        if(this.pass != null) {
            /* Send a PASS message first */
            IRCMessage passMessage = null;
            if(this.pass != null) {
                passMessage = new IRCMessage(IRCMessageType.PASS, this.pass);
            }
            
            connection.sendMessages(passMessage, nickMessage, userMessage);
        } else {
            connection.sendMessages(nickMessage, userMessage);
        }
    }

    @Override
    public void onClose(IRCChannelHandler connection) throws Exception {
        /*
         * With the underly connection closed we can safely shutdown the thread
         * pool
         */
        System.out.println("IRCClient.onClose");
        synchronized(this.handlers) {
            this.threadPool.shutdown();
        }
    }
    
    @Override 
    public void onShutdown() {
        this.bootstrap.releaseExternalResources();
    }

    @Override
    public void receiveMessage(IRCChannelHandler connection, IRCMessage message) throws Exception {
        switch(message.getType()) {
        
        /* At least one of these should be received once a good connection is established */
        case RPL_ENDOFMOTD:
        case RPL_LUSERME:
        case RPL_LUSERCHANNELS:
        case RPL_LUSERCLIENT:
        case RPL_LUSEROP:
        case RPL_LUSERUNKNOWN:
            synchronized(this.connected) {
                if(this.connected.isSet() == false) {
                    this.connected.set();
                    this.connectedSuccessfully = true;
                }
            }
            break;
       
        /* Error while connecting */
        case ERR_NICKNAMEINUSE:
            synchronized(this.connected) {
                if(this.connected.isSet() == false) {
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

        synchronized(this.handlers) {
            if(this.threadPool.isShutdown()) {
                return;
            }
            
            for(IRCMessageFilter filter : this.handlersKeys) {
                if(filter.check(message)) {
                    IRCMessageHandler handler = this.handlers.get(filter);
                    MessageHandlerRunnable runnableHandler = new MessageHandlerRunnable(handler, message);
                    this.threadPool.submit(runnableHandler);
                }
            }
        }
    }

    public void addHandler(IRCMessageFilter filter, IRCMessageHandler handler) {
        synchronized(this.handlers) {
            this.handlers.put(filter, handler);
            this.handlersKeys.add(filter);
        }
    }

    public boolean removeHandler(IRCMessageFilter filter) {
        synchronized(this.handlers) {
            if(this.handlers.containsKey(filter)) {
                this.handlers.remove(filter);
                this.handlersKeys.remove(filter);
                return true;
            }

            return false;
        }
    }

    public IRCChannelHandler getConnection() {
        return this.channelHandler;
    }
}
