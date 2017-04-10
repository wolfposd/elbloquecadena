package com.elbloquecadena.storage;

import java.util.ArrayList;
import java.util.List;

import com.elbloquecadena.p2p.Peer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Settings {

    @SerializedName("listenport")
    @Expose
    public final int listenport;
    @SerializedName("publickey")
    @Expose
    public final byte[] publickey;
    @SerializedName("privatekey")
    @Expose
    public final byte[] privatekey;
    @SerializedName("peer")
    @Expose
    public final List<Peer> peer = new ArrayList<>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Settings() {
        listenport = 8080;
        publickey = null;
        privatekey = null;
    }

    public Settings(int listenport, byte[] publickey, byte[] privatekey, List<Peer> peer) {
        this.listenport = listenport;
        this.publickey = publickey;
        this.privatekey = privatekey;
        this.peer.addAll(peer);
    }

}