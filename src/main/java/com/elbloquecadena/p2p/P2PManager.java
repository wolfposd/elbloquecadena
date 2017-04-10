package com.elbloquecadena.p2p;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.google.protobuf.ByteString;

public class P2PManager implements MessagesHandler, ServerEvents {

    private ExecutorService cachedPool = Executors.newCachedThreadPool();

    private P2PServer server;

    public P2PManager(int serverPort, List<Peer> initialPeers, KeyPair keypair) {
        server = new P2PServer(serverPort, this, this);

        cachedPool.submit(server::start);

        initialPeers.forEach(peer -> {
            AddressBook.getInstance().addPeer(peer);
            System.out.println("Starting P2P client connecting to: " + peer.getHostAddress() + ":" + peer.getPortNumber());
            P2PClient cli = new P2PClient(peer.getHostAddress(), peer.getPortNumber(), keypair, this);
            cachedPool.submit(cli::open);
        });
    }

    private void sendPeerDiscoveryMessageTo(Socket endpoint) {
        try {
            MsgPeerDiscovery peerdisc = MsgPeerDiscovery.newBuilder().setMsgid(Crypto.randomString(15)).build();
            Message m = Message.newBuilder().setPeerdiscovery(peerdisc).build();
            System.out.println("sending peerdisc " + peerdisc.getMsgid() + " to " + endpoint.getInetAddress() + ":" + endpoint.getPort());
            m.writeDelimitedTo(endpoint.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onPing(MsgPing ping, Socket endpoint) {
        System.out.println("received Ping, answering Pong");
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

        System.out.println("Should i add Peer to AB? \n    " + peer);

        // AddressBook.getInstance().addPeer(peer);
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
        // Peer p = new Peer(clientSocket);
        //
        // if (AddressBook.getInstance().lastSeen(p) != AddressBook.PEER_NEVER_SEEN) {
        // AddressBook.getInstance().addPeer(p);
        // } else {
        // AddressBook.getInstance().updateLastSeen(p);
        // }

        sendPeerDiscoveryMessageTo(clientSocket);
    }

}
