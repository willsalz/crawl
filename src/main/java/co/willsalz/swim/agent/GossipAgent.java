package co.willsalz.swim.agent;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
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
    private static final Duration PROTOCOL_PERIOD = Duration.ofMillis(100);
    private static final int NUM_PROXIES = 3;

    private final Channel channel;
    private final GossipHandler handler;
    private final Integer port;
    private final Random rng = new Random(System.currentTimeMillis());
    private final Peers peers;
    private final Timer timer = new HashedWheelTimer();

    public GossipAgent(final List<InetSocketAddress> peers) throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();
        final Bootstrap b = new Bootstrap()
            .group(group)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .handler(new GossipPipeline(this));

        this.channel = b.bind(0).sync().channel();
        this.handler = channel.pipeline().get(GossipHandler.class);
        this.port = ((InetSocketAddress) channel.localAddress()).getPort();
        this.peers = new Peers(peers, rng);
    }

    public Peers peers() {
        return peers;
    }

    public Integer port() {
        return port;
    }

    public void ping() {
        final Optional<InetSocketAddress> peer = this.peers.choice();
        if (peer.isPresent()) {
            timer.newTimeout(
                new PingTimeout(this, peer.get(), logger),
                PROTOCOL_PERIOD.toMillis(),
                TimeUnit.MILLISECONDS
            );

            handler.sendPing(peer.get());
        }
    }

    public void pingReq(final InetSocketAddress target) {
        final List<InetSocketAddress> peers = this.peers.sample(NUM_PROXIES);
        peers.remove(target);
        if (!peers.isEmpty()) {
            for (final InetSocketAddress peer : peers) {
                handler.sendPingReq(peer, target);
            }
        }
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

    public Integer getPort() {
        return port;
    }

}
