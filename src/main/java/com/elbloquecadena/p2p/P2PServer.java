package com.elbloquecadena.p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

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

    public P2PServer() {
        this.serverPortNumber = DEFAULT_SERVER_PORT;
    }

    public P2PServer(int portNumber) {
        this.serverPortNumber = portNumber;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(serverPortNumber)) {
            while (true) {
                System.out.println("waiting for accept connection");
                Socket clientSocket = serverSocket.accept();
                new Thread(new SocketHandler(new Peer(clientSocket))).start();
                System.out.println("started new connection");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SocketHandler implements Runnable {
        private final int threadNumber;
        private final Peer peer;

        public SocketHandler(Peer peer) {
            this.peer = peer;
            this.threadNumber = runningThreads.getAndIncrement();
        }

        public void run() {

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(peer.getSocket().getInputStream()))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    System.out.println("read line: " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
