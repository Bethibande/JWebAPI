package com.bethibande.web.processors;

import com.bethibande.web.WebRequest;
import com.bethibande.web.annotations.Path;

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
