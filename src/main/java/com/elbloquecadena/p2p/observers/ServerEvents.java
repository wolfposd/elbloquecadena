package com.elbloquecadena.p2p.observers;

import java.net.Socket;

public interface ServerEvents {

    public void onClientConnectsToServer(Socket clientSocket);

}
