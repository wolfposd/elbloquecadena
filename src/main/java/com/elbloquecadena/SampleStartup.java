package com.elbloquecadena;

import java.security.KeyPair;
import java.util.ArrayList;

import org.bouncycastle.jce.interfaces.ECPublicKey;

import com.elbloquecadena.conversion.JSON;
import com.elbloquecadena.crypto.Crypto;
import com.elbloquecadena.p2p.Peer;
import com.elbloquecadena.storage.Settings;
import com.elbloquecadena.storage.SettingsPeer;
import com.elbloquecadena.storage.block.ImmutableBlock;
import com.elbloquecadena.storage.block.MutableBlock;
import com.github.jtmsp.merkletree.byteable.ByteableString;

public class SampleStartup {

    public static void main(String[] args) {

        KeyPair kp = Crypto.generateKeys();
        byte[] pubke22y = ((ECPublicKey) kp.getPublic()).getQ().getEncoded(true);
        Settings settings = new Settings(8080,pubke22y, kp.getPrivate().getEncoded(), new ArrayList<Peer>());
        System.out.println(settings.privatekey.length);
        System.out.println(JSON.toJSON(settings));
        if (1 == (2 - 1)) {
            System.exit(0);
        }

        MutableBlock<ByteableString> mutGenesis = new MutableBlock<>(0);
        mutGenesis.addTransaction(new ByteableString("This is the Genesis Block"));

        ImmutableBlock genesis = mutGenesis.makeImmutable(new byte[] {});

        // System.out.println(JSON.toJSON(genesis));

        MutableBlock<ByteableString> nextBlock = new MutableBlock<>(1);
        nextBlock.addTransaction(new ByteableString("another one down"));
        nextBlock.addTransaction(new ByteableString("More to Come"));
        nextBlock.addTransaction(new ByteableString("this is gud"));

        KeyPair keypair = Crypto.generateKeys();

        byte[] pubkey = ((ECPublicKey) keypair.getPublic()).getQ().getEncoded(true);

        byte[] signed = Crypto.signMessage(keypair.getPrivate(), nextBlock.getRootHash());

        nextBlock.addValidatorHash(pubkey, signed);

        System.out.println("can verify?" + Crypto.verifySignature(pubkey, nextBlock.getRootHash(), signed));

        ImmutableBlock imBlock1 = nextBlock.makeImmutable(genesis.blockHash);

        System.out.println(JSON.toJSON(imBlock1));

        String jsonBlock1 = JSON.toJSON(imBlock1);

        ImmutableBlock imblock1Copy = JSON.fromJSON(jsonBlock1, ImmutableBlock.class);

        System.out.println(JSON.toJSON(imblock1Copy));

        System.out.println(imBlock1.equals(imblock1Copy));

    }

}
