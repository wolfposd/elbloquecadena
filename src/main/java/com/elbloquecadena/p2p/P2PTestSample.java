package com.elbloquecadena.p2p;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import com.elbloquecadena.conversion.JSON;
import com.elbloquecadena.crypto.Crypto;
import com.elbloquecadena.crypto.CryptoException;
import com.elbloquecadena.storage.Settings;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class P2PTestSample {

    public static void main(String[] args) throws CryptoException, FileNotFoundException, InterruptedException {

        String settingsPath = "settings.json";
        if (args.length > 0) {
            settingsPath = args[0];
        }

        Settings settings = JSON.fromJson(new FileReader(settingsPath), Settings.class);

        System.out.println(settings.listenport);
        System.out.println(settings.peer);
        System.out.println(Base64.getEncoder().encodeToString(settings.privatekey));
        System.out.println(Base64.getEncoder().encodeToString(settings.publickey));

        PublicKey pubkey = Crypto.getPublicKeyFromBytes(settings.publickey);
        PrivateKey privkey = Crypto.getPrivateKeyFromBytes(settings.privatekey);

        KeyPair k = new KeyPair(pubkey, privkey);

        P2PManager manager = new P2PManager(settings.listenport, settings.peer, k);

        while (true) {
            Thread.sleep(2000);
        }

    }

}
