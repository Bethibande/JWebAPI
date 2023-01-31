package com.bethibande.web.processors;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.types.Request;

import java.lang.reflect.Method;

public class MethodInvocationHandlerAdapter implements MethodInvocationHandler {

    @Override
    public void beforeInvocation(Method method, Request request, JWebAPI api) { }

    @Override
    public void afterInvocation(Method method, Request request, JWebAPI api) { }
}
