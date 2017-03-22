package com.elbloquecadena.p2p;

public class P2PTestSample {

    public static void main(String[] args) throws InterruptedException {

        P2PServer server = new P2PServer();

        System.out.println("starting server");
        new Thread(server::start).start();

        System.out.println("Waiting for server");
        Thread.sleep(1000);
        
        
        
        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            System.out.println("stopping server");
            server.stop();
        }).start();
        

        new Thread(() -> {
            System.out.println("starting client");
            P2PClient client = new P2PClient("localhost", P2PServer.DEFAULT_SERVER_PORT);
            Peer peer = client.open();
        }).start();

        while (true) {
            Thread.sleep(2000);
        }

    }

}
