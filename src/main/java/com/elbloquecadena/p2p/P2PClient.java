package com.elbloquecadena.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.elbloquecadena.messages.Messages.Message;
import com.elbloquecadena.p2p.observers.MessageDelivery;
import com.elbloquecadena.p2p.observers.MessageReceiver;

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
    private final MessageReceiver mReceiver;

    private MessageDelivery delivery;

    public P2PClient(String hostAddress, int port, MessageReceiver receiver, MessageDelivery delivery) {
        this.hostAddress = hostAddress;
        this.destinationPort = port;
        this.mReceiver = receiver;
        this.delivery = delivery;
    }

    public Peer start() {
        boolean result = tryOpen(20);
        if (result) {
            delivery.sendMsgHello(socket, null);
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
                delivery.sendMsgPing(socket, null);
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
                if (mReceiver != null)
                    mReceiver.onMessageReceived(m, this.socket);
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
}
