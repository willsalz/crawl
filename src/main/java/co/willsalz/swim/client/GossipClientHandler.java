package co.willsalz.swim.client;

import java.net.InetSocketAddress;

import co.willsalz.swim.generated.Gossip;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;

public class GossipClientHandler extends SimpleChannelInboundHandler<AddressedEnvelope<Gossip.Message, InetSocketAddress>> {

    private Channel channel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AddressedEnvelope<Gossip.Message, InetSocketAddress> msg) throws Exception {
        System.err.println(msg);
        ChannelFuture future = ctx.writeAndFlush(
            new DefaultAddressedEnvelope<>(
                Gossip.Message.newBuilder()
                    .setType(Gossip.Message.Type.ACK)
                    .setAck(Gossip.Ack.newBuilder().build())
                    .build(),
                msg.sender(),
                ctx.channel().localAddress()
            )
        );
        future.addListener(ChannelFutureListener.CLOSE);
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

    public ChannelFuture ping(final InetSocketAddress recipient) {
        return channel.writeAndFlush(
            new DefaultAddressedEnvelope<>(
                Gossip.Message.newBuilder()
                    .setType(Gossip.Message.Type.PING)
                    .setPing(Gossip.Ping.newBuilder().build())
                    .build(),
                recipient,
                channel.localAddress()
            )
        );

    }
}
