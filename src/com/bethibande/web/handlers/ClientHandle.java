package com.bethibande.web.handlers;

import java.lang.reflect.Method;
import java.util.List;

public class ClientHandle {

    private final Method method;
    private final HandleType type;
    private final String uri;

    private final List<Integer> queryFields;

    public ClientHandle(Method method, HandleType type, String uri, List<Integer> queryFields) {
        this.method = method;
        this.type = type;
        this.uri = uri;
        this.queryFields = queryFields;
    }

    public List<Integer> getQueryFields() {
        return queryFields;
    }

    public Method getMethod() {
        return method;
    }

    public HandleType getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }
}
