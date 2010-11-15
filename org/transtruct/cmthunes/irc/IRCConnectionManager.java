package org.transtruct.cmthunes.irc;

import org.transtruct.cmthunes.irc.messages.*;
import org.transtruct.cmthunes.irc.protocol.*;

public interface IRCConnectionManager {
    public void onConnect(IRCChannelHandler connection) throws Exception;

    public void onClose(IRCChannelHandler connection) throws Exception;
    
    public void onShutdown();

    public void receiveMessage(IRCChannelHandler connection, IRCMessage message) throws Exception;
}
