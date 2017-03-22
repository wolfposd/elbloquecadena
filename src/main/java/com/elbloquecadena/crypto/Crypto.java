package com.elbloquecadena.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;

import com.elbloquecadena.storage.block.ImmutableBlock;

public class Crypto {

    private static final String PROVIDER = "BC";
    private static final String ECDSA = "ECDSA";
    private static final String SHA256_WITH_ECDSA = "SHA256withECDSA";
    private static final String CURVE_NAME = "curve25519";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair generateKeys() {
        try {
            X9ECParameters ecP = CustomNamedCurves.getByName(CURVE_NAME);
            ECParameterSpec ecSpec = new ECParameterSpec(ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(), ecP.getSeed());

            KeyPairGenerator keygen = KeyPairGenerator.getInstance(ECDSA, PROVIDER);
            keygen.initialize(ecSpec, new SecureRandom());
            return keygen.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
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

    public static PublicKey getPublicKeyFromBytes(byte[] pubKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        X9ECParameters ecP = CustomNamedCurves.getByName(CURVE_NAME);
        ECParameterSpec ecSpec = new ECParameterSpec(ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(), ecP.getSeed());
        KeyFactory kf = KeyFactory.getInstance(ECDSA, PROVIDER);
        ECNamedCurveSpec params = new ECNamedCurveSpec(CURVE_NAME, ecSpec.getCurve(), ecSpec.getG(), ecSpec.getN());
        ECPoint point = ECPointUtil.decodePoint(params.getCurve(), pubKey);
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);
        return kf.generatePublic(pubKeySpec);
    }

    public static boolean verifySignature(byte[] compressedPublicKey, byte[] message, byte[] signature) {
        try {
            PublicKey publicKey = getPublicKeyFromBytes(compressedPublicKey);
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
