package com.elbloquecadena.p2p.async.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public class AsyncServer {

    private Set<Attachment> openConnections = new HashSet<>();

    public AsyncServer(String bindIpAddress, int bindPort) throws IOException {
        InetSocketAddress sockAddr = new InetSocketAddress(bindIpAddress, bindPort);

        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(sockAddr);

        Attachment attachment = new Attachment(server, null, ByteBuffer.allocate(2048), false);

        server.accept(attachment, new ConnectionHandler());
    }

    public void writeToClient(SocketAddress addr, String message) {

        openConnections.stream().filter(t -> addr.equals(t.clientAddress)).forEach(a -> {
            Attachment m = new Attachment(a.server, a.client, ByteBuffer.wrap(message.getBytes()), false);
            a.client.write(m.buffer, m, new ReadWriteHandler());
        });

    }

    class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Attachment> {

        @Override
        public void completed(AsynchronousSocketChannel client, Attachment att) {
            att.server.accept(att, this);

            Attachment newAttachment = new Attachment(att.server, client, ByteBuffer.allocate(2048), true);
            openConnections.add(newAttachment);

            client.read(newAttachment.buffer, newAttachment, new ReadWriteHandler());
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            System.err.println("Failed to accept a connection from: " + attachment);
            exc.printStackTrace();
        }

    }

    class ReadWriteHandler implements CompletionHandler<Integer, Attachment> {
        @Override
        public void completed(Integer result, Attachment attachment) {
            if (result == -1) {
                try {
                    attachment.client.close();
                    System.out.format("Stopped listening to the client %s%n", attachment.clientAddress);
                    openConnections.remove(attachment);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (attachment.isRead) {
                attachment.buffer.flip();
                int limits = attachment.buffer.limit();
                byte bytes[] = new byte[limits];
                attachment.buffer.get(bytes, 0, limits);
                Charset cs = Charset.forName("UTF-8");
                String msg = new String(bytes, cs);
                System.out.format("Client at  %s  says: %s%n", attachment.clientAddress, msg);
                attachment.isRead = false; // It is a write
                attachment.buffer.rewind();

            } else {
                // Write to the client
                attachment.client.write(attachment.buffer, attachment, this);
                attachment.isRead = true;
                attachment.buffer.clear();
                attachment.client.read(attachment.buffer, attachment, this);
            }
        }

        @Override
        public void failed(Throwable e, Attachment attach) {
            e.printStackTrace();
        }
    }

}
