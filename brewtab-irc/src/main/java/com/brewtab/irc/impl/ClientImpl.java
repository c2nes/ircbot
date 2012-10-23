package com.brewtab.irc.impl;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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

    public static final int DEFAULT_PORT = 6667;
    public static final int DEFAULT_SSL_PORT = 6697;

    /* Default SSL context, initialized lazily */
    private static SSLContext defaultSSLContext = null;

    /* SSLContext used by this client, may be specified externally */
    private SSLContext sslContext = null;

    /* Netty objects */
    private ClientBootstrap bootstrap;

    /* Cached copy of the server's name */
    private String servername;

    /* Connection information */
    private String password;
    private String nick;
    private String username;
    private String hostname;
    private String realName;

    private ConnectionImpl connection;

    private boolean connected;

    private boolean usingSSL;

    /**
     * Construct a new IRCClient connected to the given address
     * 
     * @param address The address to connect to
     */
    public ClientImpl() {
        this.connection = new ConnectionImpl();

        this.password = null;
        this.nick = null;
        this.username = null;
        this.hostname = null;
        this.realName = null;

        this.connected = false;
        this.usingSSL = false;

        this.connection.addMessageListener(
            MessageFilters.message(MessageType.PING, (String) null),
            new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    connection.send(new Message(MessageType.PONG, message.getArgs()));
                }
            });
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

    public void setSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public SSLContext getSSLContext() {
        if (sslContext == null) {
            sslContext = getDefaultSSLContext();
        }

        return sslContext;
    }

    private SslHandler getClientSSLHandler() {
        SSLContext context = getSSLContext();
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(true);

        return new SslHandler(engine);
    }

    private URI parseConnectURISpec(String uriSpec) {
        final URI uri;

        try {
            uri = new URI(uriSpec);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (uri.getScheme().toLowerCase().equals("irc")) {
            usingSSL = false;
        } else if (uri.getScheme().toLowerCase().equals("ircs")) {
            usingSSL = true;
        } else {
            throw new ConnectionException("protocol must be one of irc or ircs");
        }

        int port = uri.getPort();

        if (port == -1) {
            if (usingSSL) {
                port = DEFAULT_SSL_PORT;
            } else {
                port = DEFAULT_PORT;
            }
        }

        try {
            return new URI(uri.getScheme(), null, uri.getHost(), port, null, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException("unexpected exception", e);
        }
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
     * Connect with a password and with the given information
     */
    @Override
    public void connect(String uriSpec) {
        URI uri = parseConnectURISpec(uriSpec);

        ChannelFactory channelFactory = new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());

        ChannelPipeline clientPipeline = NettyChannelPipeline.newPipeline(connection);
        SslHandler sslHandler = null;

        if (usingSSL) {
            sslHandler = getClientSSLHandler();
            clientPipeline.addFirst("ssl", sslHandler);
        }

        bootstrap = new ClientBootstrap(channelFactory);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setPipeline(clientPipeline);

        /* Perform connection */
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));

        log.debug("connecting");

        try {
            future.await();
        } catch (InterruptedException e) {
            throw new ConnectionException("Interrupted while connecting");
        }

        if (future.isSuccess()) {
            log.debug("connected successfully");

            if (usingSSL) {
                doSSLHandshake(sslHandler);
            }

            connected = true;
            registerConnection();
        } else {
            log.debug("connection failed");
            throw new ConnectionException(future.getCause());
        }
    }

    private void registerConnection() {
        log.debug("registering connection");

        Message nickMessage = new Message(MessageType.NICK, this.nick);
        Message userMessage = new Message(MessageType.USER,
            this.username, this.hostname, this.servername, this.realName);

        if (this.password != null) {
            /* Send a PASS message first */
            connection.send(new Message(MessageType.PASS, this.password));
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
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        if (connected) {
            throw new IllegalStateException("can't set password, already connected");
        }

        this.password = password;
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

        this.nick = nick;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        if (connected) {
            throw new IllegalStateException("can't set username, already connected");
        }

        this.username = username;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public void setHostname(String hostname) {
        if (connected) {
            throw new IllegalStateException("can't set hostname, already connected");
        }

        this.hostname = hostname;
    }

    @Override
    public String getRealName() {
        return realName;
    }

    @Override
    public void setRealName(String realName) {
        if (connected) {
            throw new IllegalStateException("can't set real name, already connected");
        }

        this.realName = realName;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
