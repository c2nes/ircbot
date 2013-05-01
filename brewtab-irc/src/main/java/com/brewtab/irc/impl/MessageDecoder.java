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
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.messages.InvalidMessageException;
import com.brewtab.irc.messages.Message;

/**
 * Decode a String into an IRCMessage object. The String should be a newline
 * terminated line formatted as it would be as part of an IRC connection.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 * @see <a href="http://www.irchelp.org/irchelp/rfc/rfc.html">
 *      http://www.irchelp.org/irchelp/rfc/rfc.html</a>
 */
class MessageDecoder extends OneToOneDecoder {
    private static final Logger log = LoggerFactory.getLogger(MessageDecoder.class);

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) {
        String message = (String) msg;

        try {
            return Message.fromString(message);
        } catch (InvalidMessageException e) {
            log.warn("Received unknown/invalid message: {}", e.getMessage());
            return null;
        }
    }
}
