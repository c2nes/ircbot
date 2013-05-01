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

        /*
         * Build pipeline. The first handler in the pipeline is the first
         * handler for in-bound messages and the last handler for out-bound
         * messages.
         */

        pipeline.addLast("frameDecoder", getFrameDecoder());
        pipeline.addLast("stringDecoder", new StringDecoder());
        pipeline.addLast("stringEncoder", new StringEncoder());
        pipeline.addLast("ircDecoder", new MessageDecoder());
        pipeline.addLast("ircEncoder", new MessageEncoder());
        pipeline.addLast("ircConnectionHandler", connectionHandler);

        return pipeline;
    }
}
