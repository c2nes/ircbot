package org.transtruct.cmthunes.irc.protocol;

import org.transtruct.cmthunes.irc.*;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.*;
import org.jboss.netty.handler.codec.string.*;
import org.jboss.netty.util.*;

/**
 * A ChannelPipelineFactory for an IRCConnection. This pipeline works with an
 * IRCChannelHandler which in turn wraps an IRCChannelManager to send and
 * receive IRCMessage objects. The rest of the pipelines is responsible for
 * encoding/decoding these objects to/from the underly text stream.
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public class IRCChannelPipelineFactory implements ChannelPipelineFactory {
    /** Connection manager factory */
    private IRCConnectionManagerFactory connectionManagerFactory;

    /** Connection manager */
    private IRCConnectionManager connectionManager;

    /**
     * Initialize a new IRCChannelPipelineFactory using the given
     * IRCConnectionManagerFactory to create IRCConnectionManager objects for
     * each new pipeline
     * 
     * @param connectionManagerFactory
     *            The high level manager factory of the connection. Usually an
     *            IRCServerClientFactory
     */
    public IRCChannelPipelineFactory(IRCConnectionManagerFactory connectionManagerFactory) {
        this.connectionManagerFactory = connectionManagerFactory;
        this.connectionManager = null;
    }

    /**
     * Initialize a new IRCChannelPipelineFactory using the given
     * IRCConnectionManager for all new pipelines
     * 
     * @param connectionManager
     *            The high level manager for all created pipelines
     */
    public IRCChannelPipelineFactory(IRCConnectionManager connectionManager) {
        this.connectionManagerFactory = null;
        this.connectionManager = connectionManager;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        /* Frame decoder. Splits stream by \r\n terminated lines */
        byte[] delimiter = "\r\n".getBytes(CharsetUtil.UTF_8);
        ChannelBuffer delimiterBuffer = ChannelBuffers.copiedBuffer(delimiter);
        ChannelHandler frameDecoder = new DelimiterBasedFrameDecoder(1024, delimiterBuffer);
        IRCConnectionManager channelManager = this.connectionManager;
        if(channelManager == null) {
            channelManager = this.connectionManagerFactory.getConnectionManager();
        }

        /* Build pipeline */
        pipeline.addLast("frameDecoder", frameDecoder);
        pipeline.addLast("stringDecoder", new StringDecoder());
        pipeline.addLast("stringEncoder", new StringEncoder());
        pipeline.addLast("ircDecoder", new IRCMessageDecoder());
        pipeline.addLast("ircEncoder", new IRCMessageEncoder());
        pipeline.addLast("ircConnectionHandler", new IRCChannelHandler(channelManager));

        return pipeline;
    }
}
