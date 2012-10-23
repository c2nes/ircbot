package com.brewtab.irc.client;

import com.brewtab.irc.impl.ClientFactoryImpl;

public abstract class ClientFactory {
    public static final int DEFAULT_PORT = 6667;
    public static final int DEFAULT_SSL_PORT = 6697;

    public static final String DEFAULT_USERNAME = "irc-client";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final String DEFAULT_REALNAME = "Brewtab IRC Client";

    public abstract void setUsername(String username);

    public abstract void setHostname(String hostname);

    public abstract void setRealName(String realName);

    public abstract void setNick(String nick);

    public abstract Client connect(String uri);

    public abstract Client connect(String uri, String password);

    public static ClientFactory newInstance() {
        return ClientFactoryImpl.newInstance();
    }
}
