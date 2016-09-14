package co.willsalz.swim.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

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

public class GossipHandler extends SimpleChannelInboundHandler<AddressedEnvelope<Gossip.Message, InetSocketAddress>> {

    private final Logger logger = LoggerFactory.getLogger("gossip-handler");
    private final List<InetSocketAddress> peers;
    private final Random rng = new Random();
    private Channel channel;

    public GossipHandler(List<InetSocketAddress> peers) {
        this.peers = peers;
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

    public ChannelFuture ping() {

        final InetSocketAddress peer = peers.get(rng.nextInt(peers.size()));
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
}
