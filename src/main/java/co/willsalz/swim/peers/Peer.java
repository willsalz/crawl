package co.willsalz.swim.peers;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

public class Peer {
    private final InetSocketAddress address;
    private AtomicReference<PeerState> state;

    public Peer(final InetSocketAddress address) {
        this.address = address;
        this.state.set(PeerState.Unknown);
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public AtomicReference<PeerState> getState() {
        return state;
    }
}
