package co.willsalz.swim.peers;

import java.net.InetSocketAddress;

public class Peer {
    private final InetSocketAddress address;
    private PeerState peerState;

    public Peer(final InetSocketAddress address) {
        this.address = address;
        this.peerState = PeerState.Alive;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public PeerState getPeerState() {
        return peerState;
    }

    public Peer setPeerState(PeerState peerState) {
        this.peerState = peerState;
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        return address.equals(obj);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}

