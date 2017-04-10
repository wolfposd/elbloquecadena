package com.elbloquecadena.p2p;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class AddressBook {

    private static final AddressBook instance;

    public static final long PEER_NEVER_SEEN = -1;

    private Map<Peer, Long> peers = new HashMap<>();

    static {
        instance = new AddressBook();
    }

    private AddressBook() {
    }

    public static AddressBook getInstance() {
        return instance;
    }

    public void addPeer(Peer p) {
        peers.put(p, System.currentTimeMillis());
    }

    public void removePeer(Peer p) {
        peers.remove(p);
    }

    public void updateLastSeen(Peer p) {
        peers.merge(p, System.currentTimeMillis(), (first, second) -> {
            return System.currentTimeMillis();
        });
    }

    public void updateLastSeen(Socket sock) {
        updateLastSeen(new Peer(sock));
    }

    public long lastSeen(Peer p) {
        return peers.getOrDefault(p, PEER_NEVER_SEEN);
    }

    public void forEach(BiConsumer<Peer, Long> action) {
        peers.forEach(action);
    }

}
