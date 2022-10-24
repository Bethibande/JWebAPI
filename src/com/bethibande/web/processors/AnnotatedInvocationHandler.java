package com.bethibande.web.processors;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.WebRequest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public abstract class AnnotatedInvocationHandler<T extends Annotation> implements MethodInvocationHandler {

    private final Class<T> type;

    public AnnotatedInvocationHandler(Class<T> type) {
        this.type = type;
    }

    public void beforeInvocation(Method method, T annotation, WebRequest request, JWebServer server) { }
    public void afterInvocation(Method method, T annotation, WebRequest request, JWebServer server) { }

    @Override
    public void beforeInvocation(Method method, WebRequest request, JWebServer server) {
        if(!method.isAnnotationPresent(type)) return;

        beforeInvocation(method, method.getAnnotation(type), request, server);
    }

    @Override
    public void afterInvocation(Method method, WebRequest request, JWebServer server) {
        if(!method.isAnnotationPresent(type)) return;

        afterInvocation(method, method.getAnnotation(type), request, server);
    }
}
