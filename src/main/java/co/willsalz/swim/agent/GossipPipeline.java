package co.willsalz.swim.agent;

import co.willsalz.swim.generated.Gossip;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.handler.codec.EnvelopingDatagramPacketDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.logging.LoggingHandler;

public class GossipPipeline extends ChannelInitializer<DatagramChannel> {
    private final GossipAgent agent;

    public GossipPipeline(GossipAgent agent) {
        this.agent = agent;
    }

    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ch.pipeline().addLast("loggingHandler", new LoggingHandler());
        ch.pipeline().addLast("udpDecoder", new EnvelopingDatagramPacketDecoder(new ProtobufDecoder(Gossip.Message.getDefaultInstance())));
        ch.pipeline().addLast("udpEncoder", new DatagramPacketEncoder<>(new ProtobufEncoder()));
        ch.pipeline().addLast("clientHandler", new GossipHandler(agent));
    }
}
