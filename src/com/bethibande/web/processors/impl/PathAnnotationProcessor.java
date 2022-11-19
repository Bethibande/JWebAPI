package com.bethibande.web.processors.impl;

import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.AnnotationProcessor;
import com.bethibande.web.annotations.Path;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class PathAnnotationProcessor extends AnnotationProcessor<Path> {

    public PathAnnotationProcessor() {
        super(Path.class, true);
    }

    @Override
    public Object accept(ServerContext context, Path annotation, Executable executable, Parameter parameter) {
        return context.request().getUri().getPath();
    }
}
