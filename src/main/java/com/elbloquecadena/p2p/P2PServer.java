package com.elbloquecadena.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.elbloquecadena.messages.Messages.Message;
import com.elbloquecadena.messages.Messages.MsgPing;
import com.elbloquecadena.messages.Messages.MsgPong;
import com.elbloquecadena.p2p.observers.MessagesHandler;
import com.elbloquecadena.p2p.observers.ServerEvents;

/**
 * Peer2Peer server socket class
 * 
 * @author wolfposd
 *
 */
public class P2PServer {

    public static final int DEFAULT_SERVER_PORT = 61000;

    private final int serverPortNumber;

    private final static AtomicInteger runningThreads = new AtomicInteger();

    private List<SocketHandler> connectedPeers = new ArrayList<>();

    private List<ServerSocket> serverSockets = new ArrayList<>();

    private AtomicBoolean keepAcceptingConnections = new AtomicBoolean(true);

    private MessagesHandler mHandler;

    private ServerEvents serverEvents;

    public P2PServer() {
        this.serverPortNumber = DEFAULT_SERVER_PORT;
    }

    public P2PServer(int portNumber, MessagesHandler mHandler, ServerEvents serverEvents) {
        this.serverPortNumber = portNumber;
        this.mHandler = mHandler;
        this.serverEvents = serverEvents;
    }

    public void start() {
        keepAcceptingConnections.set(true);
        try (ServerSocket serverSocket = new ServerSocket(serverPortNumber)) {
            while (keepAcceptingConnections.get()) {
                System.out.println("waiting for accept connection");

                if (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    SocketHandler handler = new SocketHandler(new Peer(clientSocket));
                    connectedPeers.add(handler);
                    new Thread(handler).start();
                    serverSockets.add(serverSocket);
                    System.out.println("started new connection");
                    serverEvents.onClientConnectsToServer(clientSocket);
                }
            }
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public void stop() {
        keepAcceptingConnections.set(false);
        serverSockets.forEach(s -> {
            try {
                s.close();
            } catch (IOException e) {
            }
        });
        serverSockets.clear();

        connectedPeers.forEach(handler -> {
            try {
                handler.continueLoop = false;
                handler.peer.getSocket().close();

            } catch (IOException e) {
            }
        });
        connectedPeers.clear();
    }

    private class SocketHandler implements Runnable {
        private final Peer peer;
        public boolean continueLoop = true;

        public SocketHandler(Peer peer) {
            this.peer = peer;
        }

        public void run() {

            try {
                InputStream inStream = peer.getSocket().getInputStream();
                while (continueLoop && !peer.getSocket().isClosed()) {
                    Message m = Message.parseDelimitedFrom(inStream);
                    if (mHandler != null)
                        mHandler.onMessageReceived(m, peer.getSocket());
                }
            } catch (Exception e) {
                // continueLoop = false;
                // e.printStackTrace();
            }
            System.out.println("I was done reading anyway :-(");
        }
    }

    public void handlePing(MsgPing ping, OutputStream outstream) {
        System.out.println("received Ping, answering Pong");
        MsgPong.Builder pong = MsgPong.newBuilder().setMsgid(ping.getMsgid());
        try {
            Message.newBuilder().setPong(pong).build().writeDelimitedTo(outstream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
