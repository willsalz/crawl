package co.willsalz.swim.agent;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import co.willsalz.swim.generated.Gossip;
import co.willsalz.swim.peers.PeerState;

public class Peers {
    private final Set<InetSocketAddress> peers = new HashSet<>();
    private final Map<InetSocketAddress, PeerState> peerState = new HashMap<>();
    private final Set<InetSocketAddress> pendingAck = new HashSet<>();
    private final Map<InetSocketAddress, List<InetSocketAddress>> pendingPing = new HashMap<>();
    private final Random rng;

    public Peers(final List<InetSocketAddress> peers, final Random rng) {
        for (final InetSocketAddress peer : peers) {
            this.peers.add(peer);
            this.peerState.put(peer, PeerState.Alive);
        }
        this.rng = rng;
    }

    public Optional<InetSocketAddress> choice() {
        if (peers.isEmpty()) {
            return Optional.empty();
        }
        final List<InetSocketAddress> peerList = peers.stream().collect(Collectors.toList());
        return Optional.of(peerList.get(rng.nextInt(peerList.size())));
    }

    public List<InetSocketAddress> sample(final int k) {
        if (peers.isEmpty()) {
            return Collections.emptyList();
        }
        final List<InetSocketAddress> peerList = peers.stream().collect(Collectors.toList());
        return rng.ints(0, peerList.size())
            .limit(k)
            .distinct()
            .mapToObj(peerList::get)
            .collect(Collectors.toList());
    }

    public synchronized void handleAck(final InetSocketAddress sender, final Gossip.Ack ack) {
        peers.add(sender);
        pendingAck.remove(sender);
        peerState.put(sender, PeerState.Alive);
    }

    public synchronized void handlePing(final InetSocketAddress sender, final Gossip.Ping ping) {
        peers.add(sender);
        peerState.put(sender, PeerState.Alive);
    }

    public synchronized void handlePingReq(final InetSocketAddress sender, final Gossip.PingReq pingReq) {

        peers.add(sender);
        peerState.put(sender, PeerState.Alive);

        final InetSocketAddress target = new InetSocketAddress(
            pingReq.getTarget().getAddress(),
            pingReq.getTarget().getPort()
        );

        pendingPing.merge(
            sender,
            Collections.singletonList(target),
            (peers, peer) -> {
                peers.addAll(peer);
                return peers;
            }
        );

    }

}
