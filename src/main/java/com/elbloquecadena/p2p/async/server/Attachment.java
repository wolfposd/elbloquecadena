package com.elbloquecadena.p2p.async.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Objects;

public class Attachment {
    public final AsynchronousServerSocketChannel server;
    public final AsynchronousSocketChannel client;
    public final ByteBuffer buffer;
    public SocketAddress clientAddress;
    public boolean isRead;

    public Attachment(AsynchronousServerSocketChannel server, AsynchronousSocketChannel client, ByteBuffer buffer, boolean isRead) {

        this.server = server;
        this.client = client;
        this.buffer = buffer;
        try {
            if (client != null) {
                this.clientAddress = client.getRemoteAddress();
            }
        } catch (IOException e) {
            this.clientAddress = null;
            e.printStackTrace();
        }
        this.isRead = isRead;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Attachment) {
            Attachment other = (Attachment) obj;
            return Objects.equals(server, other.server) && Objects.equals(client, other.client)
                    && Objects.equals(clientAddress, other.clientAddress);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return client.hashCode();
    }
}