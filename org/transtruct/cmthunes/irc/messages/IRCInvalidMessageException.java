package org.transtruct.cmthunes.irc.messages;

/**
 * Throw if an attempt is made to construct an invalid message. A message can be
 * invalid if the wrong number of arguments are given in its construction
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public class IRCInvalidMessageException extends IllegalArgumentException {
    private static final long serialVersionUID = 7155869076162198882L;

    /**
     * Construct a new IRCInvalidMessageException using a default message
     */
    public IRCInvalidMessageException() {
        super("Invalid message format");
    }

    /**
     * Construct a new IRCInvalidMessageException using the given message
     * 
     * @param reason
     *            The message to store with the exception
     */
    public IRCInvalidMessageException(String reason) {
        super(reason);
    }
}
