package com.bethibande.web.processors;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.types.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public abstract class AnnotatedInvocationHandler<T extends Annotation> implements MethodInvocationHandler {

    private final Class<T> type;

    public AnnotatedInvocationHandler(Class<T> type) {
        this.type = type;
    }

    public void beforeInvocation(Method method, T annotation, Request request, JWebAPI server) { }
    public void afterInvocation(Method method, T annotation, Request request, JWebAPI server) { }

    @Override
    public void beforeInvocation(Method method, Request request, JWebAPI api) {
        if(!method.isAnnotationPresent(type)) return;

        beforeInvocation(method, method.getAnnotation(type), request, api);
    }

    @Override
    public void afterInvocation(Method method, Request request, JWebAPI api) {
        if(!method.isAnnotationPresent(type)) return;

        afterInvocation(method, method.getAnnotation(type), request, api);
    }
}
