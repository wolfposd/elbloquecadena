package com.elbloquecadena.storage.block;

public class ValidatorAndHash {
    public final byte[] p;
    public final byte[] s;

    public ValidatorAndHash(byte[] pubkey, byte[] signature) {
        this.p = pubkey;
        this.s = signature;
    }

    public byte[] getPublicKey() {
        return p;
    }

    public byte[] getSignature() {
        return s;
    }

}