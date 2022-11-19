package com.bethibande.web.processors.impl;

import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.AnnotationProcessor;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.annotations.HeaderValue;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class HeaderValueAnnotationProcessor extends AnnotationProcessor<HeaderValue> {

    public HeaderValueAnnotationProcessor() {
        super(HeaderValue.class, true);
    }

    @Override
    public Object accept(ServerContext context, HeaderValue annotation, Executable executable, Parameter parameter) {
        final Class<?> type = parameter.getType();
        final WebRequest request = context.request();

        if(Collection.class.isAssignableFrom(type)) {
            return request.getRequestHeaders().get(annotation.header());
        }

        List<String> values = request.getRequestHeaders().get(annotation.header());
        if(values == null || values.isEmpty()) return null;
        String value = values.get(0);

        if(type == String.class || type == CharSequence.class) {
            return value;
        }
        if(type == Byte.class || type == byte.class) {
            return Byte.parseByte(value);
        }
        if(type == Short.class || type == short.class) {
            return Short.parseShort(value);
        }
        if(type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        }
        if(type == Long.class || type == long.class) {
            return Long.parseLong(value);
        }
        if(type == Float.class || type == float.class) {
            return Float.parseFloat(value);
        }
        if(type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        }
        if(type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if(Date.class.isAssignableFrom(type)) {
            return LocalDateTime.parse(value);
        }

        return null;
    }
}
