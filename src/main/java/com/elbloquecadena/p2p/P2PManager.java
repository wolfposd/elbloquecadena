package com.elbloquecadena.p2p;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.elbloquecadena.crypto.Crypto;
import com.elbloquecadena.crypto.CryptoException;
import com.elbloquecadena.messages.Messages;
import com.elbloquecadena.messages.Messages.MPeer;
import com.elbloquecadena.messages.Messages.Message;
import com.elbloquecadena.messages.Messages.MsgHello;
import com.elbloquecadena.messages.Messages.MsgPeerDiscovery;
import com.elbloquecadena.messages.Messages.MsgPeerExchange;
import com.elbloquecadena.messages.Messages.MsgPing;
import com.elbloquecadena.messages.Messages.MsgPing.Builder;
import com.elbloquecadena.messages.Messages.MsgPong;
import com.elbloquecadena.p2p.observers.MessageDelivery;
import com.elbloquecadena.p2p.observers.MessageReceiver;
import com.elbloquecadena.p2p.observers.ServerEvents;
import com.elbloquecadena.storage.Settings;
import com.google.protobuf.ByteString;

public class P2PManager implements MessageReceiver, MessageDelivery, ServerEvents {

    private ExecutorService cachedPool = Executors.newCachedThreadPool();

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private P2PServer server;

    private Settings settings;

    private KeyPair keypair;

    public P2PManager(Settings settings) {
        this.settings = settings;
        try {
            this.keypair = Crypto.makeKeyPair(settings.privatekey, settings.publickey);
        } catch (CryptoException e) {
            e.printStackTrace();
        }
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
            P2PClient cli = new P2PClient(origPeer.getHostAddress(), origPeer.getPortNumber(), this, this);
            cachedPool.submit(cli::start);
        } else {
            System.out.println("connectiong to peer already open ,peer:" + peer);
        }
    }

    protected void onPing(MsgPing ping, Socket endpoint) {
        sendMsgPong(endpoint, ping.getMsgid());
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

        sendMsgPeerExchange(endpoint, peerdisc.getMsgid());
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
        System.err.println("Got a message, which doesnt match any known types");
        System.err.println("From: " + endpoint.getInetAddress().getHostAddress() + ":" + endpoint.getPort());
        System.err.println("Message: " + message);
    }

    @Override
    public void sendMsgPing(Socket socket, String msgid) {
        if (msgid == null)
            msgid = Crypto.randomString(15);

        try {
            Builder pingBuilder = MsgPing.newBuilder().setMsgid(msgid);
            Message.newBuilder().setPing(pingBuilder).build().writeDelimitedTo(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMsgPeerDiscovery(Socket socket, String msgid) {
        if (msgid == null)
            msgid = Crypto.randomString(15);

        try {
            MsgPeerDiscovery.Builder peerdisc = MsgPeerDiscovery.newBuilder().setMsgid(msgid);
            Message m = Message.newBuilder().setPeerdiscovery(peerdisc).build();
            m.writeDelimitedTo(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMsgHello(Socket socket, String msgid) {
        if (msgid == null)
            msgid = Crypto.randomString(15);

        try {
            ByteString pubkey = ByteString.copyFrom(Crypto.compressedKey(keypair.getPublic()));
            String bs = Base64.getEncoder().encodeToString(pubkey.toByteArray());
            System.err.println("Sending Hello: " + this.settings.listenport + " pub:" + bs);

            MPeer myself = MPeer.newBuilder().setHost("localhost").setPort(settings.listenport).setPubkey(pubkey).build();
            MsgHello.Builder hello = MsgHello.newBuilder().setMyself(myself).setMsgid(msgid);
            Message m = Message.newBuilder().setHello(hello).build();
            m.writeDelimitedTo(socket.getOutputStream());
        } catch (CryptoException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendMsgPeerExchange(Socket socket, String msgid) {
        if (msgid == null)
            msgid = Crypto.randomString(15);

        Messages.MsgPeerExchange.Builder pexMsg = MsgPeerExchange.newBuilder().setMsgid(msgid);

        AddressBook.getInstance().forEach((peer, ts) -> {
            System.out.println("adding peer to PexMsg" + peer.getHostAddress() + ":" + peer.getPortNumber());
            MPeer.Builder mp = MPeer.newBuilder().setHost(peer.getHostAddress()).setPort(peer.getPortNumber());
            if (peer.getPublicKey() != null) {
                mp.setPubkey(ByteString.copyFrom(peer.getPublicKey()));
            }
            pexMsg.addPeers(mp);
        });

        try {
            Message.newBuilder().setPeerexchange(pexMsg).build().writeDelimitedTo(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMsgPong(Socket socket, String msgid) {
        if (msgid == null)
            msgid = Crypto.randomString(15);

        MsgPong.Builder pong = MsgPong.newBuilder().setMsgid(msgid);
        try {
            Message.newBuilder().setPong(pong).build().writeDelimitedTo(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        sendMsgPeerDiscovery(clientSocket, null);
    }

}
