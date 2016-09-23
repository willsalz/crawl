package co.willsalz.swim.agent;

import java.net.InetSocketAddress;

import co.willsalz.swim.generated.Gossip;

public interface Handler {
    void handlePing(final InetSocketAddress sender, final Gossip.Ping ping);

    void handleAck(final InetSocketAddress sender, final Gossip.Ack ack);

    void handlePingReq(final InetSocketAddress sender, final Gossip.PingReq pingReq);
}
