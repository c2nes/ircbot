package org.transtruct.cmthunes.irc.protocol;

import org.transtruct.cmthunes.irc.messages.*;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.oneone.*;

public class IRCMessageEncoder extends OneToOneEncoder {
    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) {
        if(msg instanceof IRCMessage) {
            IRCMessage message = (IRCMessage) msg;
            return message.toString();
        } else if(msg instanceof IRCMessage[]) {
            IRCMessage[] messages = (IRCMessage[]) msg;
            StringBuffer buffer = new StringBuffer();

            for(IRCMessage message : messages) {
                buffer.append(message.toString());
            }

            return buffer.toString();
        } else {
            System.err.println("Did not receive IRCMessage to encode");
            return null;
        }
    }
}
