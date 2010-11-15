package org.transtruct.cmthunes.irc.messages;

public class IRCInvalidMessageException extends IllegalArgumentException {
    private static final long serialVersionUID = 7155869076162198882L;

    public IRCInvalidMessageException() {
        super("Invalid message format");
    }

    public IRCInvalidMessageException(String reason) {
        super(reason);
    }
}
