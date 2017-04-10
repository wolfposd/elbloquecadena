package com.elbloquecadena.console;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;

import org.bouncycastle.jce.interfaces.ECPublicKey;

import com.elbloquecadena.conversion.JSON;
import com.elbloquecadena.crypto.Crypto;
import com.elbloquecadena.p2p.Peer;
import com.elbloquecadena.storage.Settings;
import com.elbloquecadena.util.ConsoleArguments;

public class Init implements CCommands {

    @Override
    public void execute(ConsoleArguments args) {
        int port = args.getInteger("-port", 8080);

        KeyPair kp = Crypto.generateKeys();
        byte[] pubke22y = ((ECPublicKey) kp.getPublic()).getQ().getEncoded(true);

        Settings settings = new Settings(port, pubke22y, kp.getPrivate().getEncoded(), new ArrayList<Peer>());

        String outFile = args.getString("-outfile", "settings.json");

        System.out.println("Writing Settings to: " + outFile);

        JSON.toJson(settings, new File(outFile));

        System.out.println("You can add Peers to Peer[] like this:");
        System.out.println("{\"ip\":\"127.0.0.1\",\"port\":8081,\"pubkey\":\"AAABBBCCCDDDEEEFFFGGGmWw8zVZcsO6xsiXNWtp/EIa\"}");

    }

    @Override
    public String getHelpText() {
        return "Initializes the EBC\n" //
                + " Commands: \n" //
                + "  -port {8080}\n" //
                + "  -outfile {settings.json}\n\n" //
                + "Example: -init -port 1234 -outfile mysettings.json";
    }

    @Override
    public String getShortText() {
        return " to initialize default settings";
    }

}
