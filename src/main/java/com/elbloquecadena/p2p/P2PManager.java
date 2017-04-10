package com.elbloquecadena.p2p;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.elbloquecadena.messages.Messages.Message;
import com.elbloquecadena.messages.Messages.MsgPeerDiscovery;
import com.elbloquecadena.messages.Messages.MsgPeerExchange;
import com.elbloquecadena.messages.Messages.MsgPing;
import com.elbloquecadena.messages.Messages.MsgPong;

public class P2PManager implements MessagesHandler {

    private ExecutorService cachedPool = Executors.newCachedThreadPool();

    private P2PServer server;

    public P2PManager(int serverPort, List<Peer> initialPeers) {
        server = new P2PServer(serverPort, this);
        cachedPool.submit(server::start);

        initialPeers.forEach(peer -> {
            System.out.println("Starting P2P client connecting to: " + peer.getHostAddress() + ":" + peer.getPortNumber());
            P2PClient cli = new P2PClient(peer.getHostAddress(), peer.getPortNumber(), this);
            cachedPool.submit(cli::open);
        });
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
        // TODO Auto-generated method stub

    }

    protected void onPeerDiscovery(MsgPeerDiscovery peerdisc, Socket endpoint) {
        // TODO Auto-generated method stub

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
        case VALUE_NOT_SET:
        default:
            onValueNotSet(message, endpoint);
        }
    }

}
