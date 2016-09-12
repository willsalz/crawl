package co.willsalz.swim.client;

import co.willsalz.swim.generated.Gossip;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class GossipClientHandler extends SimpleChannelInboundHandler<Gossip.Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Gossip.Message msg) throws Exception {
        System.err.println(String.format("Got %s", msg));
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
