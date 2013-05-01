/*
 * Copyright (c) 2013 Christopher Thunes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.brewtab.irc.impl;

import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.Connection;
import com.brewtab.irc.ConnectionException;
import com.brewtab.irc.ConnectionStateListener;
import com.brewtab.irc.NotConnectedException;
import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.irc.client.Client;
import com.brewtab.irc.client.NickNameInUseException;
import com.brewtab.irc.messages.Message;
import com.brewtab.irc.messages.MessageListener;
import com.brewtab.irc.messages.MessageType;
import com.brewtab.irc.messages.filter.MessageFilters;

class ClientImpl implements Client {
    private static final Logger log = LoggerFactory.getLogger(ClientImpl.class);

    /* Default SSL context, initialized lazily */
    private static SSLContext defaultSSLContext = null;

    /* SSLContext used by this client, may be specified externally */
    private SSLContext sslContext = null;

    /* Netty objects */
    private ClientBootstrap bootstrap;

    /* Connection information */
    private String nick;
    private String username;
    private String hostname;
    private String realName;

    private ConnectionImpl connection;

    private boolean connected;

    /**
     * Construct a new IRCClient connected to the given address
     * 
     * @param address The address to connect to
     */
    ClientImpl() {
        this.connection = new ConnectionImpl();

        this.nick = null;
        this.username = null;
        this.hostname = null;
        this.realName = null;

        this.connected = false;

        this.connection.addMessageListener(
            MessageFilters.message(MessageType.PING, (String) null),
            new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    connection.send(new Message(MessageType.PONG, message.getArgs()));
                }
            });
    }

    void setSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    SSLContext getSSLContext() {
        if (sslContext == null) {
            sslContext = getDefaultSSLContext();
        }

        return sslContext;
    }

    private static TrustManager createNonValidatingTrustManager() {
        return new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Pass everything
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Pass everything
            }
        };
    }

    private static SSLContext getDefaultSSLContext() {
        if (defaultSSLContext == null) {
            try {
                defaultSSLContext = SSLContext.getInstance("TLS");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("runtime does not support TLS", e);
            }

            try {
                defaultSSLContext.init(null, new TrustManager[] { createNonValidatingTrustManager() }, null);
            } catch (KeyManagementException e) {
                throw new RuntimeException("failed to initialize default SSLContext", e);
            }
        }

        return defaultSSLContext;
    }

    private SslHandler getClientSSLHandler() {
        SSLContext context = getSSLContext();
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(true);

        return new SslHandler(engine);
    }

    private void doSSLHandshake(SslHandler sslHandler) {
        log.debug("performing SSL handshake");
        ChannelFuture handshakeFuture = sslHandler.handshake();

        try {
            handshakeFuture.await();
        } catch (InterruptedException e) {
            throw new ConnectionException("Interrupted while performing SSL handshake");
        }

        if (!handshakeFuture.isSuccess()) {
            throw new ConnectionException("error performing SSL handshake", handshakeFuture.getCause());
        }
    }

    /**
     * Connect to the remote server and perform the SSL handshake if SSL is being used.
     */
    void connect(SocketAddress socketAddress, boolean useSSL) {
        ChannelFactory channelFactory = new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());

        ChannelPipeline clientPipeline = NettyChannelPipeline.newPipeline(connection);
        SslHandler sslHandler = null;

        if (useSSL) {
            sslHandler = getClientSSLHandler();
            clientPipeline.addFirst("ssl", sslHandler);
        }

        bootstrap = new ClientBootstrap(channelFactory);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setPipeline(clientPipeline);

        /*
         * The connection future and the connection itself aren't perfectly
         * synchronized so we have to wait for both to acknowledge that the
         * connection has been made
         */
        final CountDownLatch connectedLatch = new CountDownLatch(1);
        ConnectionStateListener connectedListener = new ConnectionStateListener() {
            @Override
            public void onConnectionConnected() {
                connectedLatch.countDown();
            }

            @Override
            public void onConnectionClosing() {
            }

            @Override
            public void onConnectionClosed() {
            }
        };

        connection.addConnectionStateListener(connectedListener);

        try {
            /* Perform connection */
            ChannelFuture future = bootstrap.connect(socketAddress);

            log.debug("connecting");

            future.await();

            if (future.isSuccess()) {
                /* This await should be very brief */
                connectedLatch.await();

                log.debug("connected successfully");

                if (useSSL) {
                    doSSLHandshake(sslHandler);
                }

                connected = true;
            } else {
                log.debug("connection failed");
                throw new ConnectionException(future.getCause());
            }
        } catch (InterruptedException e) {
            throw new ConnectionException("Interrupted while connecting");
        } finally {
            connection.removeConnectionStateListener(connectedListener);
        }
    }

    void registerConnection(String nick, String username, String hostname, String realName, String password) {
        log.debug("registering connection");

        Message nickMessage = new Message(MessageType.NICK, nick);
        Message userMessage = new Message(MessageType.USER, username, hostname, hostname, realName);

        if (password != null) {
            /* Send a PASS message first */
            connection.send(new Message(MessageType.PASS, password));
        }

        Message response;

        try {
            response = connection.request(
                null,
                MessageFilters.any(
                    MessageFilters.message(MessageType.RPL_ENDOFMOTD),
                    MessageFilters.message(MessageType.RPL_LUSERME),
                    MessageFilters.message(MessageType.RPL_LUSERCHANNELS),
                    MessageFilters.message(MessageType.RPL_LUSERCLIENT),
                    MessageFilters.message(MessageType.RPL_LUSEROP),
                    MessageFilters.message(MessageType.RPL_LUSERUNKNOWN),
                    MessageFilters.message(MessageType.ERR_NICKNAMEINUSE)),
                nickMessage, userMessage).get(0);
        } catch (InterruptedException e) {
            throw new ConnectionException("Interrupted while awaiting connection registration response");
        }

        switch (response.getType()) {
        case ERR_NICKNAMEINUSE:
            throw new NickNameInUseException();
        default:
            log.debug("connection registered");
            break;
        }

        this.nick = nick;
        this.username = username;
        this.hostname = hostname;
        this.realName = realName;
    }

    /**
     * Quit from the server and close all network connections
     * 
     * @param reason The reason for quitting as given in the QUIT message
     */
    @Override
    public void quit(String reason) {
        if (!connected) {
            throw new NotConnectedException();
        }

        Message quit = new Message(MessageType.QUIT, reason);
        ChannelFuture future = connection.send(quit);

        /* Wait for message to be sent and then close the underlying connection */
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                connection.close();
            }
        });
    }

    @Override
    public void quit() {
        quit("Brewtab IRC client leaving");
    }

    /**
     * Join the given channel. Create a new channel object for this connection,
     * join the channel and return the IRCChannel object.
     * 
     * @param channelName The channel to join
     * @return the new IRCChannel object or null if the channel could not be
     *         joined
     */
    @Override
    public Channel join(String channelName) {
        if (!connected) {
            throw new NotConnectedException();
        }

        ChannelImpl channel = new ChannelImpl(this, channelName);

        /* Attempt to join */
        if (channel.join()) {
            return channel;
        }

        /* Error while joining */
        return null;
    }

    @Override
    public void sendMessage(User user, String message) {
        if (!connected) {
            throw new NotConnectedException();
        }

        connection.send(new Message(MessageType.PRIVMSG, user.getNick(), message));
    }

    @Override
    public String getNick() {
        return nick;
    }

    @Override
    public void setNick(String nick) {
        if (connected) {
            try {
                Message response = connection.request(
                    null,
                    MessageFilters.any(
                        MessageFilters.message(MessageType.ERR_NICKNAMEINUSE),
                        MessageFilters.message(MessageType.NICK, nick)),
                    new Message(MessageType.NICK, nick)).get(0);

                if (response.getType() == MessageType.ERR_NICKNAMEINUSE) {
                    throw new NickNameInUseException();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("request interrupted");
            }
        }
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public String getRealName() {
        return realName;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
