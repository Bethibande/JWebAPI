package com.bethibande.web.processors;

import com.bethibande.web.context.ServerContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

@SuppressWarnings("unused")
public interface ParameterFilter {

    /**
     * This filter will only apply processors to parameters with a certain annotation
     */
    static ParameterFilter annotationFilter(Class<? extends Annotation> annotation) {
        return (context, executable, parameter) -> parameter.isAnnotationPresent(annotation);
    }

    /**
     * This filter will only apply processors to parameters if the context has a valid request and session
     */
    static ParameterFilter requestFilter() {
        return (context, executable, parameter) -> context.request() != null && context.session() != null;
    }

    /**
     * This filter will ony apply processors to parameters with a certain annotation, if the context has a valid request and session
     */
    static ParameterFilter annotationRequestFilter(final Class<? extends Annotation> annotation) {
        return (context, executable, parameter) -> parameter.isAnnotationPresent(annotation) && context.request() != null && context.session() != null;
    }

    boolean filter(final ServerContext context, final Executable executable, final Parameter parameter);

}
