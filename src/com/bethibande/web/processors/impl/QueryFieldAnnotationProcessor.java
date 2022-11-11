package com.bethibande.web.processors.impl;

import com.bethibande.web.annotations.QueryField;
import com.bethibande.web.processors.AnnotationProcessor;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class QueryFieldAnnotationProcessor extends AnnotationProcessor<QueryField> {

    public QueryFieldAnnotationProcessor() {
        super(QueryField.class);
    }

    @Override
    public Object getValue(WebRequest request, QueryField annotation, Executable executable, Parameter parameter) {
        Class<?> type = parameter.getType();
        String key = annotation.value();

        if(type == boolean.class || type == Boolean.class) {
            return request.getQuery().getAsBoolean(key);
        }
        if(type == String.class || type == CharSequence.class) {
            return request.getQuery().getAsShort(key);
        }
        if(type == int.class || type == Integer.class) {
            return request.getQuery().getAsInt(key);
        }
        if(type == byte.class || type == Byte.class) {
            return request.getQuery().getAsByte(key);
        }
        if(type == short.class || type == Short.class) {
            return request.getQuery().getAsShort(key);
        }
        if(type == long.class || type == Long.class) {
            return request.getQuery().getAsLong(key);
        }
        if(type == float.class || type == Float.class) {
            return request.getQuery().getAsFloat(key);
        }
        if(type == double.class || type == Double.class) {
            return request.getQuery().getAsDouble(key);
        }

        return null;
    }

}
