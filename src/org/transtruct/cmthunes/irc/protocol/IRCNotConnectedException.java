package org.transtruct.cmthunes.irc.protocol;

/**
 * Thrown to indicate that an operation was request that could be not be
 * completed because a connection has not been established
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public class IRCNotConnectedException extends IllegalStateException {
    private static final long serialVersionUID = 4861994587138843189L;

    /**
     * Construct a new IRCNotConnectedException with the default message
     */
    public IRCNotConnectedException() {
        super("Not connected");
    }

    /**
     * Construct a new IRCNotConnectedException with the give message
     * 
     * @param reason
     *            The message to store along with the exception
     */
    public IRCNotConnectedException(String reason) {
        super(reason);
    }
}
