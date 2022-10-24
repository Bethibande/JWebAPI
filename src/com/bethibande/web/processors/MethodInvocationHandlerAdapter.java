package com.bethibande.web.processors;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Method;

public class MethodInvocationHandlerAdapter implements MethodInvocationHandler {

    @Override
    public void beforeInvocation(Method method, WebRequest request, JWebServer server) { }

    @Override
    public void afterInvocation(Method method, WebRequest request, JWebServer server) { }
}
