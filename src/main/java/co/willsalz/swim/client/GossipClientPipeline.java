package co.willsalz.swim.client;

import co.willsalz.swim.generated.Gossip;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.logging.LoggingHandler;

public class GossipClientPipeline extends ChannelInitializer<DatagramChannel> {
    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ch.pipeline().addLast("loggingHandler", new LoggingHandler());
        ch.pipeline().addLast("udpDecoder", new DatagramPacketDecoder(new ProtobufDecoder(Gossip.Message.getDefaultInstance())));
        ch.pipeline().addLast("clientHandler", new GossipClientHandler());
    }
}
