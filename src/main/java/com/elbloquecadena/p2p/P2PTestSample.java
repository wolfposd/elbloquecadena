package com.elbloquecadena.p2p;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.elbloquecadena.conversion.JSON;
import com.elbloquecadena.storage.Settings;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class P2PTestSample {

    public static void main(String[] args) throws InterruptedException, JsonSyntaxException, JsonIOException, FileNotFoundException {

        String settingsPath = "settings.json";
        if (args.length > 0) {
            settingsPath = args[0];
        }

        Settings settings = JSON.fromJson(new FileReader(settingsPath), Settings.class);

        System.out.println(settings.listenport);
        System.out.println(settings.peer);
        System.out.println(Base64.getEncoder().encodeToString(settings.privatekey));
        System.out.println(Base64.getEncoder().encodeToString(settings.publickey));

        P2PManager manager = new P2PManager(settings.listenport, settings.peer);

        while (true) {
            Thread.sleep(2000);
        }

    }

}
