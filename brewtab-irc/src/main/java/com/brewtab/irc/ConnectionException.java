package com.brewtab.irc;

public class ConnectionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConnectionException(Throwable e) {
        super(e);
    }

    public ConnectionException(String message) {
        super(message);
    }
}
