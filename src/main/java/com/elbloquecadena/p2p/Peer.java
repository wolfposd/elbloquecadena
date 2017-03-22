package com.elbloquecadena.p2p;

import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

/**
 * Peer representation
 * 
 * @author wolfposd
 */
public class Peer {

    private final String hostAddress;
    private final int portNumber;

    private Socket socket;
    private byte[] publicKey;

    public Peer(String hostAddress, int portNumber) {
        this.hostAddress = hostAddress;
        this.portNumber = portNumber;
    }

    public Peer(Socket socket) {
        this.hostAddress = socket.getInetAddress().getHostAddress();
        this.portNumber = socket.getPort();
        this.socket = socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(publicKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        else if (this == obj)
            return true;
        else if (obj instanceof Peer) {
            Peer other = (Peer) obj;
            return Objects.equals(hostAddress, other.hostAddress) //
                    && portNumber == other.portNumber //
                    // && Objects.equals(socket, other.socket) // not sure if sockets need to equal?
                    && Objects.deepEquals(publicKey, other.publicKey);
        } else
            return false;
    }

}
