package co.willsalz.swim.server;

import java.net.InetSocketAddress;

import co.willsalz.swim.generated.Gossip;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;

public class GossipServerHandler extends SimpleChannelInboundHandler<AddressedEnvelope<Gossip.Message, InetSocketAddress>> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, AddressedEnvelope<Gossip.Message, InetSocketAddress> msg) throws Exception {
        System.err.println(msg);

        switch (msg.content().getType()) {
            case PING:
                ctx.writeAndFlush(
                    new DefaultAddressedEnvelope<>(
                        Gossip.Message.newBuilder()
                            .setType(Gossip.Message.Type.ACK)
                            .setAck(Gossip.Ack.newBuilder().build())
                            .build(),
                        msg.sender(),
                        ctx.channel().localAddress()
                    )
                );
                break;
            case ACK:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
