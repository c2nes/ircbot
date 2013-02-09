package com.brewtab.irc.impl;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;


/**
 * A ChannelPipelineFactory for an IRCConnection. This pipeline works with an
 * IRCChannelHandler which in turn wraps an IRCChannelManager to send and
 * receive IRCMessage objects. The rest of the pipelines is responsible for
 * encoding/decoding these objects to/from the underly text stream.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
class NettyChannelPipelineFactory implements ChannelPipelineFactory {
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        return NettyChannelPipeline.newPipeline(new ConnectionImpl());
    }
}
