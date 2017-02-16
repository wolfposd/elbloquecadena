package com.elbloquecadena.p2p.async.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncClient {

    private Attachment attach;

    public AsyncClient(String serverAddress, int serverPort) throws InterruptedException, IOException, ExecutionException {

        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        SocketAddress serverAddr = new InetSocketAddress(serverAddress, serverPort);
        Future<Void> result = channel.connect(serverAddr);
        result.get();
        System.out.println("Connected");
        attach = new Attachment();
        attach.channel = channel;
        attach.buffer = ByteBuffer.allocate(2048);
        attach.isRead = false;
        attach.mainThread = Thread.currentThread();

        Charset cs = Charset.forName("UTF-8");
        String msg = "Hello";
        byte[] data = msg.getBytes(cs);
        attach.buffer.put(data);
        attach.buffer.flip();

        ReadWriteHandler readWriteHandler = new ReadWriteHandler();
        channel.write(attach.buffer, attach, readWriteHandler);
        attach.mainThread.join();

    }

    public void write(String message) {

        attach.channel.write(ByteBuffer.wrap(message.getBytes()), attach, new ReadWriteHandler());
    }

    class ReadWriteHandler implements CompletionHandler<Integer, Attachment> {
        @Override
        public void completed(Integer result, Attachment attach) {
            if (attach.isRead) {
                attach.buffer.flip();
                Charset cs = Charset.forName("UTF-8");
                int limits = attach.buffer.limit();
                byte bytes[] = new byte[limits];
                attach.buffer.get(bytes, 0, limits);
                String msg = new String(bytes, cs);
                System.out.format("Server Responded: " + msg);
                try {
                    msg = this.getTextFromUser();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (msg.equalsIgnoreCase("bye")) {
                    attach.mainThread.interrupt();
                    return;
                }
                attach.buffer.clear();
                byte[] data = msg.getBytes(cs);
                attach.buffer.put(data);
                attach.buffer.flip();
                attach.isRead = false; // It is a write
                attach.channel.write(attach.buffer, attach, this);
            } else {
                attach.isRead = true;
                attach.buffer.clear();
                attach.channel.read(attach.buffer, attach, this);
            }
        }

        @Override
        public void failed(Throwable e, Attachment attach) {
            e.printStackTrace();
        }

        private String getTextFromUser() throws Exception {
            return "some message" + System.currentTimeMillis();
        }
    }

}
