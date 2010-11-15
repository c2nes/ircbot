package org.transtruct.cmthunes.irc.protocol;

import org.transtruct.cmthunes.irc.messages.*;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.oneone.*;

public class IRCMessageDecoder extends OneToOneDecoder {
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) {
        String message = (String) msg;
        try {
            return IRCMessage.fromString(message);
        } catch(IRCInvalidMessageException except) {
            System.err.println("Received unknown/invalid message, " + except.getMessage());
            return null;
        }
    }
}
