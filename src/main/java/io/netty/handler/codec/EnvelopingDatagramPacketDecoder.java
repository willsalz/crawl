package io.netty.handler.codec;

import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.socket.DatagramPacket;

public class EnvelopingDatagramPacketDecoder extends DatagramPacketDecoder {
    public EnvelopingDatagramPacketDecoder(MessageToMessageDecoder<ByteBuf> decoder) {
        super(decoder);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        final List<Object> tmp = new LinkedList<>();
        super.decode(ctx, msg, tmp);
        out.add(new DefaultAddressedEnvelope<>(tmp.get(0), msg.recipient(), msg.sender()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
