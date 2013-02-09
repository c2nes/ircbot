package com.brewtab.irc.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import com.brewtab.irc.ConnectionException;
import com.brewtab.irc.client.Client;
import com.brewtab.irc.client.ClientFactory;

public class ClientFactoryImpl extends ClientFactory {
    private String username = DEFAULT_USERNAME;
    private String hostname = DEFAULT_HOSTNAME;
    private String realName = DEFAULT_REALNAME;
    private String nick = null;

    public static ClientFactory newInstance() {
        return new ClientFactoryImpl();
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }


    @Override
    public void setRealName(String realName) {
        this.realName = realName;
    }


    @Override
    public void setNick(String nick) {
        this.nick = nick;
    }

    private URI parseConnectURISpec(String uriSpec) {
        final URI uri;

        try {
            uri = new URI(uriSpec);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String scheme = uri.getScheme();

        if (scheme == null || !(scheme.equals("irc") || scheme.equals("ircs"))) {
            throw new ConnectionException("URI scheme must be one of irc or ircs");
        }

        return uri;
    }

    @Override
    public Client connect(String uriSpec) {
        return connect(uriSpec, null);
    }

    @Override
    public Client connect(String uriSpec, String password) {
        URI uri = parseConnectURISpec(uriSpec);
        boolean useSSL = uri.getScheme().equals("ircs");

        String host = uri.getHost();
        int port = uri.getPort();

        if (port == -1) {
            port = useSSL ? DEFAULT_SSL_PORT : DEFAULT_PORT;
        }

        // Use the user info from the URI if present
        String nick = this.nick;
        if (uri.getUserInfo() != null) {
            nick = uri.getUserInfo();
        }

        if (nick == null) {
            throw new ConnectionException("Nick must be provided to connect");
        }

        ClientImpl client = new ClientImpl();
        SocketAddress socketAddress = new InetSocketAddress(host, port);

        client.connect(socketAddress, useSSL);
        client.registerConnection(nick, username, hostname, realName, password);

        return client;
    }
}
