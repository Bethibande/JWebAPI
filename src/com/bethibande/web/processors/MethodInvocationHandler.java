package com.bethibande.web.processors;

import com.bethibande.web.JWebServer;
import com.bethibande.web.WebRequest;

import java.lang.reflect.Method;

public interface MethodInvocationHandler {

    void beforeInvocation(Method method, WebRequest request, JWebServer server);

    void afterInvocation(Method method, WebRequest request, JWebServer server);

}
