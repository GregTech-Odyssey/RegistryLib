package com.gto.registrylib.state;

public enum StateScope {

    WORLD("world"),
    CHUNK("chunk");

    private final String id;

    StateScope(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
