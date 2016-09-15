package co.willsalz.swim.agent;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import co.willsalz.swim.peers.Peer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GossipAgent implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger("gossip-client");
    private static final Duration PROTOCOL_PERIOD = Duration.ofSeconds(1);

    private final Channel channel;
    private final GossipHandler handler;
    private final Integer port;
    private final Set<Peer> peers = new HashSet<>();
    private final Timer timer = new HashedWheelTimer();
    private final Random rng = new Random(System.currentTimeMillis());

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
        final List<Peer> peerList = peers.stream().collect(Collectors.toList());
        final Peer peer = peerList.get(rng.nextInt(peers.size()));

        timer.newTimeout(
            new PingTimeout(this, peer),
            PROTOCOL_PERIOD.toMillis(),
            TimeUnit.MILLISECONDS
        );
        return handler.ping(peer.getAddress());
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
