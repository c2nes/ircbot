package org.transtruct.cmthunes.irc.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import org.transtruct.cmthunes.irc.messages.IRCInvalidMessageException;
import org.transtruct.cmthunes.irc.messages.IRCMessage;

/**
 * Decode a String into an IRCMessage object. The String should be a newline
 * terminated line formatted as it would be as part of an IRC connection.
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 * @see <a href="http://www.irchelp.org/irchelp/rfc/rfc.html">
 *      http://www.irchelp.org/irchelp/rfc/rfc.html</a>
 */
public class IRCMessageDecoder extends OneToOneDecoder {
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) {
        String message = (String) msg;
        try {
            return IRCMessage.fromString(message);
        } catch (IRCInvalidMessageException except) {
            System.err.println("Received unknown/invalid message, " + except.getMessage());
            return null;
        }
    }
}
