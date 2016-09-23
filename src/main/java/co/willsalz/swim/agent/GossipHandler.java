package co.willsalz.swim.agent;

import java.net.InetSocketAddress;

import co.willsalz.swim.generated.Gossip;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GossipHandler extends SimpleChannelInboundHandler<AddressedEnvelope<Gossip.Message, InetSocketAddress>> {

    private final Logger logger = LoggerFactory.getLogger("gossip-handler");
    private final Handler handler;
    private Channel channel;

    public GossipHandler(final Handler handler) {
        this.handler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AddressedEnvelope<Gossip.Message, InetSocketAddress> msg) throws Exception {

        final InetSocketAddress sender = msg.sender();
        switch (msg.content().getType()) {
            case PING:
                final Gossip.Ping ping = msg.content().getPing();
                logger.info("Ping from {}: {}", sender, ping);
                handler.handlePing(sender, ping);
                break;
            case ACK:
                final Gossip.Ack ack = msg.content().getAck();
                logger.info("Ack from {}: {}", sender, ack);
                handler.handleAck(sender, ack);
                break;
            case PING_REQ:
                final Gossip.PingReq pingReq = msg.content().getPingReq();
                logger.info("Ping Req from {}: {}", sender, pingReq);
                handler.handlePingReq(sender, pingReq);
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

}
