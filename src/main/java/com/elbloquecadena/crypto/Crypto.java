package com.elbloquecadena.crypto;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.elbloquecadena.storage.block.ImmutableBlock;

public class Crypto {

    private static final String PROVIDER = "BC";
    private static final String ECDSA = "ECDSA";
    private static final String SHA256_WITH_ECDSA = "SHA256withECDSA";

    static {
        Security.addProvider(new BouncyCastleProvider());
        // new BouncyCastleProvider().getServices().forEach(ser -> {
        // System.out.println(ser);
        // });
    }

    public static KeyPair generateKeys() {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance(ECDSA, PROVIDER);
            return keygen.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] signMessage(PrivateKey key, byte[] message) {

        try {
            Signature ecdsaSign = Signature.getInstance(SHA256_WITH_ECDSA, PROVIDER);
            ecdsaSign.initSign(key);
            ecdsaSign.update(message);
            byte[] signature = ecdsaSign.sign();
            return signature;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean verifySignature(byte[] pubkey, byte[] message, byte[] signature) {

        try {
            KeyFactory fact = KeyFactory.getInstance(ECDSA, PROVIDER);
            PublicKey publicKey = fact.generatePublic(new X509EncodedKeySpec(pubkey));
            return verifySignature(publicKey, message, signature);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifySignature(PublicKey pubkey, byte[] message, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance(SHA256_WITH_ECDSA, PROVIDER);
            ecdsaVerify.initVerify(pubkey);
            ecdsaVerify.update(message);
            boolean result = ecdsaVerify.verify(signature);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifySignatures(ImmutableBlock block) {
        for (int i = 0; i < block.signatures.length; i++) {
            boolean isVerified = Crypto.verifySignature(block.signatures[i].p, block.rootHash, block.signatures[i].s);
            if (!isVerified)
                return false;
        }
        return true;
    }

}
