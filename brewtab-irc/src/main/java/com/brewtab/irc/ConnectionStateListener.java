package com.brewtab.irc;

public interface ConnectionStateListener {
    /**
     * Called when a connection is connected.
     */
    public void onConnectionConnected();

    /**
     * Called when a connection has started being closed.
     */
    public void onConnectionClosing();

    /**
     * Called when a connection has been closed.
     */
    public void onConnectionClosed();
}
