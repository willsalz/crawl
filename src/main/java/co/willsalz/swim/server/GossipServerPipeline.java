package co.willsalz.swim.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class GossipServerPipeline extends ChannelInitializer {
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast("serverHandler", new GossipServerHandler());
    }
}
