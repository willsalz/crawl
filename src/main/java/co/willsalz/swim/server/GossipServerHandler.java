package co.willsalz.swim.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class GossipServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        System.err.println(packet);
        if ("QOTM?".equals(packet.content().toString(CharsetUtil.UTF_8))) {
            ctx.write(new DatagramPacket(
                Unpooled.copiedBuffer("QOTM: " + "foo", CharsetUtil.UTF_8), packet.sender()));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }
}

