package com.bethibande.web.processors.impl;

import com.bethibande.web.types.WebRequest;
import com.bethibande.web.annotations.Path;
import com.bethibande.web.processors.AnnotationProcessor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class PathAnnotationProcessor extends AnnotationProcessor<Path> {

    public PathAnnotationProcessor() {
        super(Path.class);
    }

    @Override
    public Object getValue(WebRequest request, Path annotation, Method method, Parameter parameter) {
        return request.getUri().getPath();
    }
}