package co.willsalz.swim.client;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

public final class GossipClient {

    static final int PORT = Integer.parseInt(System.getProperty("port", "7686"));

    public static void main(String[] args) throws Exception {

        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            final Bootstrap b = new Bootstrap()
                .group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new GossipPipeline());

            final Channel ch = b.bind(0).sync().channel();
            final GossipHandler handler = ch.pipeline().get(GossipHandler.class);
            ChannelFuture f = handler.ping(new InetSocketAddress("255.255.255.255", PORT));

            if (!ch.closeFuture().await(5000)) {
                System.err.println("QOTM request timed out.");
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
