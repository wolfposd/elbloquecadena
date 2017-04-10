package com.elbloquecadena.p2p.observers;

import java.net.Socket;

import com.elbloquecadena.messages.Messages.Message;

public interface MessagesHandler {

    public void onMessageReceived(Message message, Socket endpoint);

}