package com.brewtab.irc.client;

import com.brewtab.irc.impl.ClientFactoryImpl;

public class ClientFactory {
    public static Client newClient() {
        return ClientFactoryImpl.newClient();
    }
}
