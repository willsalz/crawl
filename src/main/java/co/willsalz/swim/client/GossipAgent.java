package co.willsalz.swim.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GossipAgent implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger("gossip-client");
    private final Channel channel;
    private final GossipHandler handler;
    private final Integer port;
    private final List<InetSocketAddress> peers = new ArrayList<>();

    public GossipAgent(final List<InetSocketAddress> peers) throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();
        final Bootstrap b = new Bootstrap()
            .group(group)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .handler(new GossipPipeline(peers));

        this.channel = b.bind(0).sync().channel();
        this.handler = channel.pipeline().get(GossipHandler.class);
        this.port = ((InetSocketAddress) channel.localAddress()).getPort();
    }

    public Integer getPort() {
        return port;
    }

    public ChannelFuture ping() {
        return handler.ping();
    }

    @Override
    public void run() {
        try {
            channel.closeFuture().await();
        } catch (InterruptedException e) {
            logger.warn("Interrupted! Shutting down.");
        } finally {
            channel.eventLoop().shutdownGracefully();
        }
    }
}
