package com.bethibande.web.examples.permission;

public enum Permissions {

    USERNAME(0);

    private final int id;

    Permissions(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
