package com.bethibande.web.processors;

import com.bethibande.web.types.WebRequest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public abstract class AnnotationProcessor<T extends Annotation> implements ParameterProcessor {

    private final Class<T> type;

    public AnnotationProcessor(Class<T> type) {
        this.type = type;
    }

    public abstract Object getValue(WebRequest request, T annotation, Executable executable, Parameter parameter);

    @Override
    public void process(WebRequest request, int parameterIndex, Executable executable, Parameter parameter) {
        if(!parameter.isAnnotationPresent(type)) return;

        request.setParameter(
                parameterIndex,
                getValue(request, parameter.getAnnotation(type), executable, parameter)
        );
    }
}
