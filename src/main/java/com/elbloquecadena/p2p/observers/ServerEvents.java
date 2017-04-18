package com.elbloquecadena.p2p.observers;

import java.net.Socket;

public interface ServerEvents {

    /**
     * Called when a ClientSocket connects to this ServerSocket
     * 
     * @param clientSocket
     */
    public void onClientConnectsToServer(Socket clientSocket);

}
