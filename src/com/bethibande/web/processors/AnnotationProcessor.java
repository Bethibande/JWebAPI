package com.bethibande.web.processors;

import com.bethibande.web.context.ServerContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public abstract class AnnotationProcessor<T extends Annotation> extends FilteredParameterProcessor {

    private final Class<T> annotation;

    public AnnotationProcessor(Class<T> annotation, boolean requiresRequest) {
        super(requiresRequest ? ParameterFilter.annotationRequestFilter(annotation): ParameterFilter.requestFilter());

        this.annotation = annotation;
    }

    public abstract Object accept(ServerContext context, T annotation, Executable executable, Parameter parameter);

    @Override
    public void accept(ServerContext context, int index, Executable executable, Parameter parameter) {
        context.request().setParameter(index, accept(context, parameter.getAnnotation(annotation), executable, parameter));
    }
}
