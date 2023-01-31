package com.bethibande.web.processors;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.types.Request;

import java.lang.reflect.Method;

public interface MethodInvocationHandler {

    void beforeInvocation(Method method, Request request, JWebAPI api);

    void afterInvocation(Method method, Request request, JWebAPI api);

}
