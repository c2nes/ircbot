package com.brewtab.irc.impl;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

class NettyChannelPipeline {
    private static ChannelHandler getFrameDecoder() {
        /* Frame decoder. Splits stream by \r\n terminated lines */
        ChannelBuffer delimiterBuffer = ChannelBuffers.wrappedBuffer(new byte[] { '\r', '\n' });
        ChannelHandler frameDecoder = new DelimiterBasedFrameDecoder(1024, delimiterBuffer);

        return frameDecoder;
    }

    public static ChannelPipeline newPipeline(ChannelHandler connectionHandler) {
        ChannelPipeline pipeline = Channels.pipeline();

        /* Build pipeline */
        pipeline.addLast("frameDecoder", getFrameDecoder());
        pipeline.addLast("stringDecoder", new StringDecoder());
        pipeline.addLast("stringEncoder", new StringEncoder());
        pipeline.addLast("ircDecoder", new MessageDecoder());
        pipeline.addLast("ircEncoder", new MessageEncoder());
        pipeline.addLast("ircConnectionHandler", connectionHandler);

        return pipeline;
    }
}
