package co.willsalz.swim.agent;

import java.net.InetSocketAddress;

import co.willsalz.swim.generated.Gossip;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static co.willsalz.swim.generated.Gossip.Message.Type.ACK;
import static co.willsalz.swim.generated.Gossip.Message.Type.PING;
import static co.willsalz.swim.generated.Gossip.Message.Type.PING_REQ;

public class GossipHandler extends SimpleChannelInboundHandler<AddressedEnvelope<Gossip.Message, InetSocketAddress>> {

    private final Logger logger = LoggerFactory.getLogger("gossip-handler");
    private final GossipAgent agent;
    private Channel channel;

    public GossipHandler(final GossipAgent agent) {
        this.agent = agent;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AddressedEnvelope<Gossip.Message, InetSocketAddress> msg) throws Exception {

        final InetSocketAddress sender = msg.sender();
        switch (msg.content().getType()) {
            case PING:
                final Gossip.Ping ping = msg.content().getPing();

                logger.info("Ping from {}: {}", sender, ping);
                agent.peers().handlePing(sender, ping);
                sendAck(sender).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                break;
            case ACK:
                final Gossip.Ack ack = msg.content().getAck();

                logger.info("Ack from {}: {}", sender, ack);
                agent.peers().handleAck(sender, ack);
                break;
            case PING_REQ:
                final Gossip.PingReq pingReq = msg.content().getPingReq();

                logger.info("Ping Req from {}: {}", sender, pingReq);
                agent.peers().handlePingReq(sender, pingReq);
                sendPing(new InetSocketAddress(pingReq.getTarget().getAddress(), pingReq.getTarget().getPort()));
                break;
            default:
                logger.error("Unknown message: {}", msg.content());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    public ChannelFuture sendAck(final InetSocketAddress peer) {

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

    public ChannelFuture sendPing(final InetSocketAddress peer) {

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

    public ChannelFuture sendPingReq(InetSocketAddress peer, InetSocketAddress target) {
        logger.info("Requesting Ping to {} via {} from {}", target, peer, channel.localAddress());

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
                peer,
                channel.localAddress()
            )
        );
    }
}
