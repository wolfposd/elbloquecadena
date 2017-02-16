package com.elbloquecadena.p2p;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.elbloquecadena.p2p.async.client.AsyncClient;
import com.elbloquecadena.p2p.async.server.AsyncServer;

public interface Peer2Peer {

    public static void main(String[] args) throws InterruptedException {

        ExecutorService cachedPool = Executors.newCachedThreadPool();

        cachedPool.execute(() -> {
            try {
                new AsyncServer("127.0.0.1", 9091);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        cachedPool.execute(() -> {
            try {
                new AsyncClient("127.0.0.1", 9091);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        while (true) {
            Thread.sleep(2000);
        }

    }

}
