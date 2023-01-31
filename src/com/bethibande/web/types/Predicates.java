package com.bethibande.web.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.function.Function;

public class Predicates {

    private static boolean isAnnotationPresent(final Parameter parameter, final Class<? extends Annotation> annotation) {
        return parameter.isAnnotationPresent(annotation);
    }

    public static Function<Parameter, Boolean> forAnnotation(final Class<? extends Annotation> annotation) {
        return (p) -> Predicates.isAnnotationPresent(p, annotation);
    }

}
