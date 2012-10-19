package com.brewtab.irc.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.brewtab.irc.messages.IRCMessage;

/**
 * Encode an IRCMessage or IRCMessage[] into a String
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public class IRCMessageEncoder extends OneToOneEncoder {
    private String encodeMessage(IRCMessage message) {
        return message.toString();
    }

    private String encodeMessages(IRCMessage[] messages) {
        StringBuilder sb = new StringBuilder();

        for (IRCMessage message : messages) {
            sb.append(encodeMessage(message));
        }

        return sb.toString();
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) {
        if (IRCMessage.class.isAssignableFrom(msg.getClass())) {
            return encodeMessage((IRCMessage) msg);
        } else if (IRCMessage[].class.isAssignableFrom(msg.getClass())) {
            return encodeMessages((IRCMessage[]) msg);
        } else {
            throw new IllegalArgumentException("msg must be one of IRCMessage or IRCMessage[]");
        }
    }
}
