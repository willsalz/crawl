package co.willsalz.swim.agent;

import java.net.InetSocketAddress;

import co.willsalz.swim.generated.Gossip;
import co.willsalz.swim.peers.Peer;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GossipHandler extends SimpleChannelInboundHandler<AddressedEnvelope<Gossip.Message, InetSocketAddress>> {

    private final Logger logger = LoggerFactory.getLogger("gossip-handler");
    private final GossipAgent agent;
    private Channel channel;

    public GossipHandler(final GossipAgent agent) {
        this.agent = agent;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AddressedEnvelope<Gossip.Message, InetSocketAddress> msg) throws Exception {

        switch (msg.content().getType()) {
            case PING:
                logger.info("Ping: {}", msg.content().getPing());
                final ChannelFuture f = ctx.writeAndFlush(
                    new DefaultAddressedEnvelope<>(
                        Gossip.Message.newBuilder()
                            .setType(Gossip.Message.Type.ACK)
                            .setAck(Gossip.Ack.newBuilder().build())
                            .build(),
                        msg.sender(),
                        ctx.channel().localAddress()
                    )
                );
                f.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                break;
            case ACK:
                logger.info("Ack: {}", msg.content().getAck());
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

    public ChannelFuture ping(final InetSocketAddress peer) {

        logger.info("Pinging {} from {}", peer, channel.localAddress());

        return channel.writeAndFlush(
            new DefaultAddressedEnvelope<>(
                Gossip.Message.newBuilder()
                    .setType(Gossip.Message.Type.PING)
                    .setPing(Gossip.Ping.newBuilder().build())
                    .build(),
                peer,
                channel.localAddress()
            )
        );

    }

    public void suspect(Peer peer) {
    }
}
