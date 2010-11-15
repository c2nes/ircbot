package org.transtruct.cmthunes.irc.protocol;

import org.transtruct.cmthunes.irc.*;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.*;
import org.jboss.netty.handler.codec.string.*;
import org.jboss.netty.util.*;

public class IRCChannelPipelineFactory implements ChannelPipelineFactory {
    private IRCConnectionManager connectionManager;
    
    public IRCChannelPipelineFactory(IRCConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        /* Frame decoder. Splits stream by \r\n terminated lines */
        byte[] delimiter = "\r\n".getBytes(CharsetUtil.UTF_8);
        ChannelBuffer delimiterBuffer = ChannelBuffers.copiedBuffer(delimiter);
        ChannelHandler frameDecoder = new DelimiterBasedFrameDecoder(1024, delimiterBuffer);

        /* Build pipeline */
        pipeline.addLast("frameDecoder", frameDecoder);
        pipeline.addLast("stringDecoder", new StringDecoder());
        pipeline.addLast("stringEncoder", new StringEncoder());
        pipeline.addLast("ircDecoder", new IRCMessageDecoder());
        pipeline.addLast("ircEncoder", new IRCMessageEncoder());
        pipeline.addLast("ircConnectionHandler", new IRCChannelHandler(this.connectionManager));
        
        return pipeline;
    }
}
