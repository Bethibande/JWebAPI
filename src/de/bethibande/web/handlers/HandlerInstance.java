package de.bethibande.web.handlers;

import java.util.HashMap;

public class HandlerInstance {

    private final String uri;
    private final Object instance;
    private final HashMap<String, MethodHandle> methods;

    public HandlerInstance(String uri, Object instance, HashMap<String, MethodHandle> methods) {
        this.uri = uri;
        this.instance = instance;
        this.methods = methods;
    }

    public String getUri() {
        return uri;
    }

    public Object getInstance() {
        return instance;
    }

    public HashMap<String, MethodHandle> getMethods() {
        return methods;
    }

}
