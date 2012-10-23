package com.brewtab.irc.impl;

import com.brewtab.irc.client.Client;

public class ClientFactoryImpl {
    public static Client newClient() {
        return new ClientImpl();
    }
}
