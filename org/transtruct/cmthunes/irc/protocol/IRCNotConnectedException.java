package org.transtruct.cmthunes.irc.protocol;

public class IRCNotConnectedException extends IllegalStateException {
    private static final long serialVersionUID = 4861994587138843189L;

    public IRCNotConnectedException() {
        super("Not connected");
    }

    public IRCNotConnectedException(String reason) {
        super(reason);
    }
}
