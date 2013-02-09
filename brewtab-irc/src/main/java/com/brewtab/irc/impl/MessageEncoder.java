package com.brewtab.irc.impl;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.brewtab.irc.messages.Message;

/**
 * Encode an IRCMessage or IRCMessage[] into a String
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
class MessageEncoder extends OneToOneEncoder {
    private String encodeMessage(Message message) {
        return message.toString();
    }

    private String encodeMessages(Message[] messages) {
        StringBuilder sb = new StringBuilder();

        for (Message message : messages) {
            sb.append(encodeMessage(message));
        }

        return sb.toString();
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) {
        if (Message.class.isAssignableFrom(msg.getClass())) {
            return encodeMessage((Message) msg);
        } else if (Message[].class.isAssignableFrom(msg.getClass())) {
            return encodeMessages((Message[]) msg);
        } else {
            throw new IllegalArgumentException("msg must be one of IRCMessage or IRCMessage[]");
        }
    }
}
