package com.bethibande.web.processors.impl;

import com.bethibande.web.annotations.QueryField;
import com.bethibande.web.processors.AnnotationProcessor;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class QueryFieldAnnotationProcessor extends AnnotationProcessor<QueryField> {

    public QueryFieldAnnotationProcessor() {
        super(QueryField.class);
    }

    @Override
    public Object getValue(WebRequest request, QueryField annotation, Method method, Parameter parameter) {
        Class<?> type = parameter.getType();



        return null;
    }

    @Override
    public void process(WebRequest request, int parameterIndex, Method method, Parameter parameter) {
        super.process(request, parameterIndex, method, parameter);
    }
}
