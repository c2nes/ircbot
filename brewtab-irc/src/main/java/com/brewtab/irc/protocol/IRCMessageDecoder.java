package com.brewtab.irc.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.messages.IRCInvalidMessageException;
import com.brewtab.irc.messages.IRCMessage;

/**
 * Decode a String into an IRCMessage object. The String should be a newline
 * terminated line formatted as it would be as part of an IRC connection.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 * @see <a href="http://www.irchelp.org/irchelp/rfc/rfc.html">
 *      http://www.irchelp.org/irchelp/rfc/rfc.html</a>
 */
public class IRCMessageDecoder extends OneToOneDecoder {
    private static final Logger log = LoggerFactory.getLogger(IRCMessageDecoder.class);

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) {
        String message = (String) msg;

        try {
            return IRCMessage.fromString(message);
        } catch (IRCInvalidMessageException e) {
            log.warn("Received unknown/invalid message: {}", e.getMessage());
            return null;
        }
    }
}
