package com.elbloquecadena.p2p;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class AddressBook {

    private static final AddressBook instance;

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
        peers.put(p, System.currentTimeMillis());
    }

    public void updateLastSeen(Socket sock) {
        peers.put(new Peer(sock), System.currentTimeMillis());
    }

    public long lastSeen(Peer p) {
        Long val = peers.get(p);
        if (val == null)
            return -1;
        else
            return val;
    }

    public void forEach(BiConsumer<Peer, Long> action) {
        peers.forEach(action);
    }

}
