package com.bethibande.web.processors;

import com.bethibande.web.context.ServerContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public abstract class AnnotationProcessor<T extends Annotation> extends FilteredParameterProcessor {

    private final Class<T> annotation;
    private final boolean requiresRequest;

    public AnnotationProcessor(Class<T> annotation, boolean requiresRequest) {
        super(ParameterFilter.annotationFilter(annotation));

        this.annotation = annotation;
        this.requiresRequest = requiresRequest;
    }

    public abstract Object accept(ServerContext context, T annotation, Executable executable, Parameter parameter);

    @Override
    public Object process(ServerContext context, Executable executable, Parameter parameter) {
        if(requiresRequest && context.session() == null) return null;
        return  accept(context, parameter.getAnnotation(annotation), executable, parameter);
    }
}
