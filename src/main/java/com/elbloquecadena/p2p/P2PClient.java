package com.elbloquecadena.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.elbloquecadena.crypto.Crypto;
import com.elbloquecadena.messages.Messages.Message;
import com.elbloquecadena.messages.Messages.MsgPing;
import com.github.jtmsp.merkletree.crypto.ByteUtil;

/**
 * Peer2Peer client socket class
 * 
 * @author wolfposd
 *
 */
public class P2PClient {

    private Socket socket;
    private String hostAddress;
    private int port;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);

    public P2PClient(String hostAddress, int port) {
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public Peer open() {
        try {
            socket = new Socket(hostAddress, port);
            socket.setSoLinger(true, 0);
            scheduleTasks();
            return new Peer(socket);
        } catch (IOException e) {
        }
        return null;
    }

    private void scheduleTasks() {
        executorService.scheduleAtFixedRate(() -> {

            System.out.println("socketclosed?" + socket.isClosed() + "  ,output open:" + socket.isOutputShutdown());
            System.out.println("socketConnected?" + socket.isConnected());

            if (!socket.isClosed()) {
                ping();
            } else {
                System.err.println("socket connection is done, canceling pings");
                throw new RuntimeException("Ping Scheduler canceled");
            }
        }, 2, 2, TimeUnit.SECONDS);

        new Thread(() -> read()).start();

    }

    private void read() {
        try {

            InputStream inStream = socket.getInputStream();
            Message m = null;
            while ((m = Message.parseDelimitedFrom(inStream)) != null) {
                switch (m.getValueCase()) {
                case PONG:
                    System.out.println("  >>  weve got a pong back:" + m.getPong().getMsgid());
                    break;
                case VALUE_NOT_SET:
                default:
                }

            }

        } catch (IOException e) {
        }

        try {
            System.err.println("Closing Socket!!! + " + hostAddress + ":" + port);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ping() {
        try {
            MsgPing ping = MsgPing.newBuilder().setMsgid(Crypto.randomString(15)).build();
            Message m = Message.newBuilder().setPing(ping).build();

            System.out.println("Ping: " + ping.getMsgid());
            m.writeDelimitedTo(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
