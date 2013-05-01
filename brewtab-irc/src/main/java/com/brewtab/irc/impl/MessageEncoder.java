/*
 * Copyright (c) 2013 Christopher Thunes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
