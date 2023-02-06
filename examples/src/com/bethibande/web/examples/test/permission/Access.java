package com.bethibande.web.examples.test.permission;

@SuppressWarnings("unused")
public enum Access {

    READ((byte)1),
    WRITE((byte)2);

    private final byte access;

    Access(byte access) {
        this.access = access;
    }

    public byte toByte() {
        return this.access;
    }

}
