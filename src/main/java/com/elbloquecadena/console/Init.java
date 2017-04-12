package com.elbloquecadena.console;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;

import com.elbloquecadena.conversion.JSON;
import com.elbloquecadena.crypto.Crypto;
import com.elbloquecadena.crypto.CryptoException;
import com.elbloquecadena.p2p.Peer;
import com.elbloquecadena.storage.Settings;
import com.elbloquecadena.util.ConsoleArguments;

public class Init implements CCommands {

    @Override
    public void execute(ConsoleArguments args) {
        try {
            int port = args.getInteger("-port", 8080);

            KeyPair kp = Crypto.generateKeys();

            Settings settings = new Settings(port, Crypto.compressedKey(kp.getPublic()), Crypto.compressedKey(kp.getPrivate()),
                    new ArrayList<Peer>());

            String outFile = args.getString("-outfile", "settings.json");

            System.out.println("Writing Settings to: " + outFile);

            JSON.toJson(settings, new File(outFile));

            System.out.println("You can add Peers to Peer[] like this:");
            System.out.println("{\"ip\":\"127.0.0.1\",\"port\":8081,\"pubkey\":\"AAABBBCCCDDDEEEFFFGGGAAABBBCCCDDDEEEFFFGGG\"}");

        } catch (CryptoException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Couldn't write to specified file");
            e.printStackTrace();
        }
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
