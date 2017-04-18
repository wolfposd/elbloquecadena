package com.elbloquecadena.p2p;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.elbloquecadena.crypto.Crypto;
import com.elbloquecadena.messages.Messages;
import com.elbloquecadena.messages.Messages.MPeer;
import com.elbloquecadena.messages.Messages.Message;
import com.elbloquecadena.messages.Messages.MsgHello;
import com.elbloquecadena.messages.Messages.MsgPeerDiscovery;
import com.elbloquecadena.messages.Messages.MsgPeerExchange;
import com.elbloquecadena.messages.Messages.MsgPing;
import com.elbloquecadena.messages.Messages.MsgPong;
import com.elbloquecadena.p2p.observers.MessagesHandler;
import com.elbloquecadena.p2p.observers.ServerEvents;
import com.elbloquecadena.storage.Settings;
import com.google.protobuf.ByteString;

public class P2PManager implements MessagesHandler, ServerEvents {

    private ExecutorService cachedPool = Executors.newCachedThreadPool();

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private P2PServer server;

    private Settings settings;

    public P2PManager(Settings settings) {
        this.settings = settings;
    }

    public void start() {
        server = new P2PServer(settings.listenport, this, this);

        cachedPool.submit(server::start);

        settings.peer.forEach(peer -> {
            connectToNewRemoteClient(settings, peer);
        });

        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Addressbook contains: " + AddressBook.getInstance());
            AddressBook.getInstance().forEach((p, l) -> {
                System.out.println("  " + p + " " + l);
            });
        }, 5, 5, TimeUnit.SECONDS);

    }

    private void connectToNewRemoteClient(Settings settings, Peer peer) {

        Peer origPeer = AddressBook.getInstance().getPeer(peer);
        if (origPeer == null) {
            origPeer = peer;
            AddressBook.getInstance().addPeer(peer);
        }

        if (origPeer.getSocket() == null) {
            System.out.println("Starting P2P client connecting to: " + origPeer.getHostAddress() + ":" + origPeer.getPortNumber());
            P2PClient cli = new P2PClient(origPeer.getHostAddress(), origPeer.getPortNumber(), settings, this);
            cachedPool.submit(cli::start);
        } else {
            System.out.println("connectiong to peer already open ,peer:" + peer);
        }
    }

    private void sendPeerDiscoveryMessageTo(Socket endpoint) {
        try {
            MsgPeerDiscovery peerdisc = MsgPeerDiscovery.newBuilder().setMsgid(Crypto.randomString(15)).build();

            System.err.println("sending peerdisc " + peerdisc.getMsgid() + " to " + endpoint.getInetAddress() + ":" + endpoint.getPort());

            Message m = Message.newBuilder().setPeerdiscovery(peerdisc).build();
            m.writeDelimitedTo(endpoint.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onPing(MsgPing ping, Socket endpoint) {
        MsgPong.Builder pong = MsgPong.newBuilder().setMsgid(ping.getMsgid());
        try {
            Message.newBuilder().setPong(pong).build().writeDelimitedTo(endpoint.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onPong(MsgPong pong, Socket endpoint) {
        AddressBook.getInstance().updateLastSeen(endpoint);
    }

    protected void onPeerExchange(MsgPeerExchange pex, Socket endpoint) {

        System.err.println("peers are: " + pex.getPeersCount());
        pex.getPeersList().forEach(peer -> {

            System.err.println("peer: " + peer.getHost() + ":" + peer.getPort() + " ->" + peer.getPubkey());

        });
    }

    protected void onPeerDiscovery(MsgPeerDiscovery peerdisc, Socket endpoint) {
        System.out.println("\n\nRECEIVED PEER DISCOVERY");

        Messages.MsgPeerExchange.Builder pexMsg = MsgPeerExchange.newBuilder();

        AddressBook.getInstance().forEach((peer, ts) -> {
            System.out.println("adding peer to PexMsg" + peer.getHostAddress() + ":" + peer.getPortNumber());
            MPeer.Builder mp = MPeer.newBuilder().setHost(peer.getHostAddress()).setPort(peer.getPortNumber());
            if (peer.getPublicKey() != null) {
                mp.setPubkey(ByteString.copyFrom(peer.getPublicKey()));
            }
            pexMsg.addPeers(mp);
        });

        try {
            Message.newBuilder().setPeerexchange(pexMsg).build().writeDelimitedTo(endpoint.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onHello(MsgHello hello, Socket endpoint) {
        Peer peer = new Peer(endpoint.getInetAddress().getHostAddress(), hello.getMyself().getPort());
        peer.setPublicKey(hello.getMyself().getPubkey().toByteArray());
        peer.setSocket(endpoint);

        if (AddressBook.getInstance().lastSeen(peer) == AddressBook.PEER_NEVER_SEEN) {
            System.out.println("AB: adding new peer " + peer);
            System.err.println("connecting to new peer " + peer);
            connectToNewRemoteClient(settings, peer);
        } else {
            AddressBook.getInstance().updateLastSeen(peer);
        }

    }

    protected void onValueNotSet(Message message, Socket endpoint) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onMessageReceived(Message message, Socket endpoint) {
        switch (message.getValueCase()) {
        case PONG:
            onPong(message.getPong(), endpoint);
            break;
        case PING:
            onPing(message.getPing(), endpoint);
            break;
        case PEERDISCOVERY:
            onPeerDiscovery(message.getPeerdiscovery(), endpoint);
            break;
        case PEEREXCHANGE:
            onPeerExchange(message.getPeerexchange(), endpoint);
            break;
        case HELLO:
            onHello(message.getHello(), endpoint);
            break;
        case VALUE_NOT_SET:
        default:
            onValueNotSet(message, endpoint);
        }
    }

    @Override
    public void onClientConnectsToServer(Socket clientSocket) {
        sendPeerDiscoveryMessageTo(clientSocket);
    }

}
