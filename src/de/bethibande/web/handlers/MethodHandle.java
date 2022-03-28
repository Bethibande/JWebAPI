package de.bethibande.web.handlers;

import java.lang.reflect.Method;

public class MethodHandle {

    private final String uri;
    private final Method method;
    private final boolean isStatic;
    private final HandleType inputType;
    private final HandleType outputType;

    public MethodHandle(String uri, Method method, boolean isStatic, HandleType inputType, HandleType outputType) {
        this.uri = uri;
        this.method = method;
        this.isStatic = isStatic;
        this.inputType = inputType;
        this.outputType = outputType;
    }

    public String getUri() {
        return uri;
    }

    public Method getMethod() {
        return method;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public HandleType getInputType() {
        return inputType;
    }

    public HandleType getOutputType() {
        return outputType;
    }
}
