package com.bethibande.web.handlers;

import com.bethibande.web.regex.RegexMatcher;

import java.lang.reflect.Method;
import java.util.Map;

public class MethodHandle {

    private final String uriRaw;
    private final String uriCompiled;
    private final Method method;
    private final boolean isStatic;
    private final HandleType inputType;
    private final HandleType outputType;

    private final Map<Integer, FieldHandle> fieldHandles;

    public MethodHandle(String uriRaw, Method method, boolean isStatic, Map<Integer, FieldHandle> handles, HandleType inputType, HandleType outputType) {
        this.uriRaw = uriRaw;
        this.uriCompiled = RegexMatcher.rawUriToRegex(uriRaw);
        this.method = method;
        this.isStatic = isStatic;
        this.fieldHandles = handles;
        this.inputType = inputType;
        this.outputType = outputType;
    }

    public String getUriRaw() {
        return uriRaw;
    }

    public String getUri() {
        return uriCompiled;
    }

    public Method getMethod() {
        return method;
    }

    public Map<Integer, FieldHandle> getFieldHandles() {
        return fieldHandles;
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
