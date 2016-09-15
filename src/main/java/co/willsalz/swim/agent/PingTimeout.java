package co.willsalz.swim.agent;

import co.willsalz.swim.peers.Peer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public class PingTimeout implements TimerTask {
    private final GossipAgent agent;
    private final Peer peer;

    public PingTimeout(final GossipAgent agent, final Peer peer) {
        this.agent = agent;
        this.peer = peer;
    }

    @Override
    public void run(final Timeout timeout) throws Exception {
        switch (peer.getState().get()) {
            case Unknown:
                agent.suspect(peer);
                break;
            case Healthy:
                break;
            case Suspected:
                break;
            case Dead:
                break;
        }
    }
}
