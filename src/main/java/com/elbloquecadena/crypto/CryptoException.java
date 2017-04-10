package com.elbloquecadena.crypto;

public class CryptoException extends Exception {

    private static final long serialVersionUID = -1805691605937541853L;

    private Exception wrapped;

    public CryptoException(Exception wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }

    public Exception getWrappedException() {
        return wrapped;
    }
}
