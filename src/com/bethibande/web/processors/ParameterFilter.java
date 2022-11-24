package com.bethibande.web.processors;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

@SuppressWarnings("unused")
public interface ParameterFilter {

    /**
     * This filter will only apply processors to parameters with a certain annotation
     */
    static ParameterFilter annotationFilter(Class<? extends Annotation> annotation) {
        return (executable, parameter) -> parameter.isAnnotationPresent(annotation);
    }

    /**
     * This filter will only apply processors to parameters of a certain type, parameter type and specified type must be equals
     * @see #typeAssignableFilter(Class)
     */
    static ParameterFilter typeFilter(Class<?> type) {
        return ((executable, parameter) -> parameter.getType().equals(type));
    }

    /**
     * This filter will only apply processors to parameters of a certain type, parameter type may be subclass of specified type.
     * Functions like instanceof using the Class.isAssignableFrom method
     */
    static ParameterFilter typeAssignableFilter(@NotNull Class<?> type) {
        return ((executable, parameter) -> type.isAssignableFrom(parameter.getType()));
    }

    boolean filter(final Executable executable, final Parameter parameter);

}
