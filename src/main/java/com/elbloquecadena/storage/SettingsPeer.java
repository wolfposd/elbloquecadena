package com.elbloquecadena.storage;

import java.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SettingsPeer {

    @SerializedName("ip")
    @Expose
    public String ip;
    @SerializedName("port")
    @Expose
    public int port;
    @SerializedName("pubkey")
    @Expose
    public byte[] pubkey;

    /**
     * No args constructor for use in serialization
     *
     */
    public SettingsPeer() {
    }

    /**
     *
     * @param port
     * @param pubkey
     * @param ip
     */
    public SettingsPeer(String ip, int port, byte[] pubkey) {
        super();
        this.ip = ip;
        this.port = port;
        this.pubkey = pubkey;
    }

    @Override
    public String toString() {
        return "SettingsPeer [ip=" + ip + ", port=" + port + ", pubkey=" + Base64.getEncoder().encodeToString(pubkey) + "]";
    }

}