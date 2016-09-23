package co.willsalz.swim.agent;

import java.net.InetSocketAddress;

import co.willsalz.swim.peers.Peer;
import co.willsalz.swim.peers.PeerState;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;

public class PingTimeout implements TimerTask {
    private final GossipAgent agent;
    private final InetSocketAddress peer;
    private final Logger logger;

    public PingTimeout(final GossipAgent agent, final InetSocketAddress peer, final Logger logger) {
        this.agent = agent;
        this.peer = peer;
        this.logger = logger;
    }

    @Override
    public void run(final Timeout timeout) throws Exception {
    }
}
