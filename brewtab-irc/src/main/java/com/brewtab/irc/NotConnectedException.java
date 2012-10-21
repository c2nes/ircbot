package com.brewtab.irc;

/**
 * Thrown to indicate that an operation was request that could be not be
 * completed because a connection has not been established
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public class NotConnectedException extends IllegalStateException {
    private static final long serialVersionUID = 4861994587138843189L;

    /**
     * Construct a new IRCNotConnectedException with the default message
     */
    public NotConnectedException() {
        super("Not connected");
    }

    /**
     * Construct a new IRCNotConnectedException with the give message
     * 
     * @param reason
     *            The message to store along with the exception
     */
    public NotConnectedException(String reason) {
        super(reason);
    }
}
