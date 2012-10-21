package com.brewtab.irc.client;

import java.net.InetSocketAddress;

import com.brewtab.irc.impl.ClientFactoryImpl;

public class ClientFactory {
    public static Client newClient(InetSocketAddress address) {
        return ClientFactoryImpl.newClient(address);
    }
}
