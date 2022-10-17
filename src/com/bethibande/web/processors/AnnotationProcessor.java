package com.bethibande.web.processors;

import com.bethibande.web.WebRequest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public abstract class AnnotationProcessor<T extends Annotation> implements ParameterProcessor {

    private final Class<T> type;

    public AnnotationProcessor(Class<T> type) {
        this.type = type;
    }

    public abstract Object getValue(WebRequest request, T annotation, Method method, Parameter parameter);

    @Override
    public void process(WebRequest request, int parameterIndex, Method method, Parameter parameter) {
        if(!parameter.isAnnotationPresent(type)) return;

        request.setParameter(
                parameterIndex,
                getValue(request, parameter.getAnnotation(type), method, parameter)
        );
    }
}
