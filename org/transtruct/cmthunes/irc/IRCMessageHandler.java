package org.transtruct.cmthunes.irc;

import org.transtruct.cmthunes.irc.messages.*;

public interface IRCMessageHandler {
    public void handleMessage(IRCMessage message);
}
