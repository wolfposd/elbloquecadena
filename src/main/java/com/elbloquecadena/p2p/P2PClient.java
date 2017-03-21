package com.elbloquecadena.p2p;

import java.io.IOException;
import java.net.Socket;

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

    public P2PClient(String hostAddress, int port) {
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public Peer open() {
        try {
            this.socket = new Socket(hostAddress, port);
            return new Peer(socket);
        } catch (IOException e) {
        }
        return null;
    }

    public void write(String s) {
        try {
            socket.getOutputStream().write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
