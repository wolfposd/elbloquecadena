package com.elbloquecadena.p2p.observers;

import java.net.Socket;

import com.elbloquecadena.util.annotation.NotNull;
import com.elbloquecadena.util.annotation.Nullable;

public interface MessageDelivery {

    /**
     * Send Ping to socket
     * 
     * @param socket
     *            {@link NotNull}
     * @param msgid
     *            {@link Nullable}
     */
    public void sendMsgPing(@NotNull Socket socket, @Nullable String msgid);

    /**
     * Send Pong to socket, usually answer to a Ping
     * 
     * @param socket
     *            {@link NotNull}
     * @param msgid
     *            {@link Nullable}
     */
    public void sendMsgPong(@NotNull Socket socket, @Nullable String msgid);

    /**
     * Send a Hello Msg containing on listenport and pubkey
     * 
     * @param socket
     *            {@link NotNull}
     * @param msgid
     *            {@link Nullable}
     */
    public void sendMsgHello(@NotNull Socket socket, @Nullable String msgid);

    /**
     * Ask for a PeerExchange from another peer
     * 
     * @param socket
     *            {@link NotNull}
     * @param msgid
     *            {@link Nullable}
     */
    public void sendMsgPeerDiscovery(@NotNull Socket socket, @Nullable String msgid);

    /**
     * Answer to a peerdiscovery, with a list of peers
     * 
     * @param socket
     *            {@link NotNull}
     * @param msgid
     *            {@link Nullable}
     */
    public void sendMsgPeerExchange(@NotNull Socket socket, @Nullable String msgid);

}
