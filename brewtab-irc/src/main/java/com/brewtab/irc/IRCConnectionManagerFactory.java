package com.brewtab.irc;

/**
 * A factory for IRCConnectionManager objects. An IRCConnectionManagerFactory
 * can be used to provide separate IRCConnectionManager objects per pipeline.
 * Typical usage case would be to provide separate managers to each client
 * connection for a server.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public interface IRCConnectionManagerFactory {
    /**
     * Return a new IRCConnectionManager
     * 
     * @return the new connection manager
     */
    public IRCConnectionManager getConnectionManager();
}
