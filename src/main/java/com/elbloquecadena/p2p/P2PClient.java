package com.elbloquecadena.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.elbloquecadena.crypto.Crypto;
import com.elbloquecadena.crypto.CryptoException;
import com.elbloquecadena.messages.Messages.MPeer;
import com.elbloquecadena.messages.Messages.Message;
import com.elbloquecadena.messages.Messages.MsgHello;
import com.elbloquecadena.messages.Messages.MsgPeerDiscovery;
import com.elbloquecadena.messages.Messages.MsgHello.Builder;
import com.elbloquecadena.messages.Messages.MsgPing;
import com.elbloquecadena.p2p.observers.MessagesHandler;
import com.elbloquecadena.storage.Settings;
import com.google.protobuf.ByteString;

/**
 * Peer2Peer client socket class
 * 
 * @author wolfposd
 *
 */
public class P2PClient {

    public static final int PING_SCHEDULE_SECONDS = 5;

    private Socket socket;
    private final String hostAddress;
    private final int destinationPort;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
    private final MessagesHandler handler;

    private KeyPair keypair;
    private final Settings settings;

    public P2PClient(String hostAddress, int port, Settings settings, MessagesHandler handler) {
        this.hostAddress = hostAddress;
        this.destinationPort = port;
        this.handler = handler;
        this.settings = settings;

        try {
            this.keypair = Crypto.makeKeyPair(settings.privatekey, settings.publickey);
        } catch (CryptoException e) {
        }
    }

    public Peer open() {
        boolean result = tryOpen(20);
        if (result) {
            sendHelloMessage(socket);
            return new Peer(socket);
        } else {
            return null;
        }
    }

    private boolean tryOpen(int times) {
        int i = 0;
        boolean result = false;

        while (i < times) {
            System.out.println("connection try " + i + " -> " + hostAddress + ":" + destinationPort);
            try {
                socket = new Socket(hostAddress, destinationPort);
                socket.setSoLinger(true, 0);
                scheduleTasks();

                result = true;
                break;
            } catch (IOException e) {
                i++;
                System.err.println(e.getMessage());

                try {
                    Thread.sleep(100 + 600 * i);
                } catch (InterruptedException e1) {
                }
            }
        }

        return result;
    }

    private void scheduleTasks() {
        executorService.scheduleAtFixedRate(() -> {
            // System.out.println("socketclosed?" + socket.isClosed() + " ,output open:" + socket.isOutputShutdown());
            // System.out.println("socketConnected?" + socket.isConnected());
            if (!socket.isClosed()) {
                sendPingMessage();
            } else {
                System.err.println("socket connection is done, canceling pings");
                throw new RuntimeException("Ping Scheduler canceled");
            }
        }, PING_SCHEDULE_SECONDS, PING_SCHEDULE_SECONDS, TimeUnit.SECONDS);

        new Thread(() -> read()).start();

    }

    private void read() {
        try {
            InputStream inStream = socket.getInputStream();
            Message m = null;
            while ((m = Message.parseDelimitedFrom(inStream)) != null) {
                if (handler != null)
                    handler.onMessageReceived(m, this.socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.err.println("Closing Socket!!! + " + hostAddress + ":" + destinationPort);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHelloMessage(Socket socket) {

        try {
            ByteString pubkey = ByteString.copyFrom(Crypto.compressedKey(keypair.getPublic()));
            MPeer myself = MPeer.newBuilder().setHost("localhost").setPort(settings.listenport).setPubkey(pubkey).build();

            String bs = Base64.getEncoder().encodeToString(pubkey.toByteArray());

            System.err.println("Sending Hello: " + this.destinationPort + " pub:" + bs);

            Builder hello = MsgHello.newBuilder().setMyself(myself);

            Message m = Message.newBuilder().setHello(hello).build();

            m.writeDelimitedTo(socket.getOutputStream());
        } catch (CryptoException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendPingMessage() {
        try {
            MsgPing ping = MsgPing.newBuilder().setMsgid(Crypto.randomString(15)).build();
            Message m = Message.newBuilder().setPing(ping).build();
            m.writeDelimitedTo(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPeerDiscovery() {
        try {
            MsgPeerDiscovery peerdisc = MsgPeerDiscovery.newBuilder().setMsgid(Crypto.randomString(15)).build();
            System.err.println("sending peerdisc " + peerdisc.getMsgid() + " to " + socket.getInetAddress() + ":" + socket.getPort());
            Message m = Message.newBuilder().setPeerdiscovery(peerdisc).build();
            m.writeDelimitedTo(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
