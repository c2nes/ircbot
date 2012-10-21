package com.brewtab.irc.impl;

import java.net.InetSocketAddress;

import com.brewtab.irc.client.Client;

public class ClientFactoryImpl {
    public static Client newClient(InetSocketAddress address) {
        return new ClientImpl(address);
    }
}
