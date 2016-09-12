package co.willsalz.swim.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.logging.LoggingHandler;

public class GossipServerPipeline extends ChannelInitializer<DatagramChannel> {
    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ch.pipeline().addLast("loggingHandler", new LoggingHandler());
        ch.pipeline().addLast("udpEncoder", new DatagramPacketEncoder<>(new ProtobufEncoder()));
        ch.pipeline().addLast("serverHandler", new GossipServerHandler());
    }
}
