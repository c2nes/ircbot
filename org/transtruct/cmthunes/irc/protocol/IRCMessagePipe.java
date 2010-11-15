package org.transtruct.cmthunes.irc.protocol;

import org.transtruct.cmthunes.irc.messages.*;

import org.jboss.netty.channel.*;

public interface IRCMessagePipe {
    public ChannelFuture sendMessage(IRCMessage message) throws IRCNotConnectedException;

    public ChannelFuture sendMessages(IRCMessage... messages) throws IRCNotConnectedException;
}
