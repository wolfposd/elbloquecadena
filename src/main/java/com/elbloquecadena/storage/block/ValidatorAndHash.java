package com.elbloquecadena.storage.block;

import java.util.Objects;

public class ValidatorAndHash {
    /** Public Key */
    public final byte[] p;
    /** Signature */
    public final byte[] s;

    public ValidatorAndHash(byte[] pubkey, byte[] signature) {
        this.p = pubkey;
        this.s = signature;
    }

    /**
     * Returns the Compressed Public Key
     */
    public byte[] getPublicKey() {
        return p;
    }

    /**
     * Returns the signature of the data
     */
    public byte[] getSignature() {
        return s;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ValidatorAndHash) {
            ValidatorAndHash other = (ValidatorAndHash) obj;
            return Objects.deepEquals(p, other.p) && Objects.deepEquals(s, other.s);
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return p.hashCode() + s.hashCode();
    }

}