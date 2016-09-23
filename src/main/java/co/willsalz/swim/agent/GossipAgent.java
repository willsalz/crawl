package co.willsalz.swim.agent;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

import co.willsalz.swim.collections.Sample;
import co.willsalz.swim.generated.Gossip;
import co.willsalz.swim.peers.PeerState;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static co.willsalz.swim.generated.Gossip.Message.Type.ACK;
import static co.willsalz.swim.generated.Gossip.Message.Type.PING;
import static co.willsalz.swim.generated.Gossip.Message.Type.PING_REQ;

public final class GossipAgent implements Runnable, Handler {

    private static final Logger logger = LoggerFactory.getLogger("gossip-client");
    private static final Duration PROTOCOL_PERIOD = Duration.ofMillis(100);
    private static final int NUM_PROXIES = 3;

    private final Channel channel;
    private final Integer port;
    private final Random rng = new Random(System.currentTimeMillis());
    private final Timer timer = new HashedWheelTimer();
    private final Map<InetSocketAddress, PeerState> peers = new ConcurrentHashMap<>();
    private final Set<InetSocketAddress> pendingAck = new ConcurrentSkipListSet<>();
    private final Map<InetSocketAddress, List<InetSocketAddress>> pendingPing = new ConcurrentHashMap<>();

    public GossipAgent(final List<InetSocketAddress> peers) throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();
        final Bootstrap b = new Bootstrap()
            .group(group)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .handler(new GossipPipeline(this));

        this.channel = b.bind(0).sync().channel();
        this.port = ((InetSocketAddress) channel.localAddress()).getPort();
        for (final InetSocketAddress peer : peers) {
            this.peers.put(peer, PeerState.Alive);
        }

    }

    public Integer port() {
        return port;
    }

    public void ping() {
        final Optional<InetSocketAddress> peer = Sample.choice(peers.keySet(), rng);
        if (peer.isPresent()) {
            timer.newTimeout(
                new PingTimeout(this, peer.get(), logger),
                PROTOCOL_PERIOD.toMillis(),
                TimeUnit.MILLISECONDS
            );

            sendPing(peer.get());
        }
    }

    public void pingReq(final InetSocketAddress target) {
        final List<InetSocketAddress> proxies = Sample.sample(peers.keySet(), NUM_PROXIES, rng);
        proxies.remove(target);
        if (!proxies.isEmpty()) {
            for (final InetSocketAddress proxy : proxies) {
                sendPingReq(proxy, target);
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

    @Override
    public void handleAck(final InetSocketAddress sender, final Gossip.Ack ack) {
        pendingAck.remove(sender);
        peers.put(sender, PeerState.Alive);
    }

    @Override
    public void handlePing(final InetSocketAddress sender, final Gossip.Ping ping) {
        peers.put(sender, PeerState.Alive);
        sendAck(sender).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public void handlePingReq(final InetSocketAddress sender, final Gossip.PingReq pingReq) {

        peers.put(sender, PeerState.Alive);

        final InetSocketAddress target = new InetSocketAddress(
            pingReq.getTarget().getAddress(),
            pingReq.getTarget().getPort()
        );

        pendingPing.merge(
            target,
            Collections.singletonList(sender),
            (peers, peer) -> {
                peers.addAll(peer);
                return peers;
            }
        );

        sendPing(new InetSocketAddress(pingReq.getTarget().getAddress(), pingReq.getTarget().getPort()));

    }

    private ChannelFuture sendAck(final InetSocketAddress peer) {

        logger.info("Acking {} from {}", peer, channel.localAddress());

        return channel.writeAndFlush(
            new DefaultAddressedEnvelope<>(
                Gossip.Message.newBuilder()
                    .setType(ACK)
                    .setAck(Gossip.Ack.newBuilder().build())
                    .build(),
                peer,
                channel.localAddress()
            )
        );

    }

    private ChannelFuture sendPing(final InetSocketAddress peer) {

        logger.info("Pinging {} from {}", peer, channel.localAddress());

        return channel.writeAndFlush(
            new DefaultAddressedEnvelope<>(
                Gossip.Message.newBuilder()
                    .setType(PING)
                    .setPing(Gossip.Ping.newBuilder().build())
                    .build(),
                peer,
                channel.localAddress()
            )
        );

    }

    private ChannelFuture sendPingReq(InetSocketAddress proxy, InetSocketAddress target) {
        logger.info("Requesting Ping to {} via {} from {}", target, proxy, channel.localAddress());

        return channel.writeAndFlush(
            new DefaultAddressedEnvelope<>(
                Gossip.Message.newBuilder()
                    .setType(PING_REQ)
                    .setPingReq(
                        Gossip.PingReq.newBuilder()
                            .setTarget(
                                Gossip.Peer.newBuilder()
                                    .setAddress(target.getHostString())
                                    .setPort(target.getPort())
                            )
                    )
                ,
                proxy,
                channel.localAddress()
            )
        );
    }

}
