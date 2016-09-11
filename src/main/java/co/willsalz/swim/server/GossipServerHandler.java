package co.willsalz.swim.server;

import co.willsalz.swim.generated.Gossip;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class GossipServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        System.err.println(packet);
        ctx.write(
            new DefaultAddressedEnvelope<>(
                Gossip.Message.newBuilder()
                    .setType(Gossip.Message.Type.ACK)
                    .setAck(Gossip.Ack.newBuilder().build())
                    .build(),
                packet.sender()
            )
        );
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
