package co.willsalz.swim.client;

import co.willsalz.swim.decoders.MessageDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DatagramPacketDecoder;

public class GossipClientPipeline extends ChannelInitializer {
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast("clientHandler", new GossipClientHandler());
        ch.pipeline().addLast("frameDecoder", new DatagramPacketDecoder(new MessageDecoder()));
    }
}
