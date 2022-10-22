package com.bethibande.web.processors;

import com.bethibande.web.WebRequest;
import com.bethibande.web.annotations.HeaderValue;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class HeaderValueAnnotationProcessor extends AnnotationProcessor<HeaderValue> {

    public HeaderValueAnnotationProcessor() {
        super(HeaderValue.class);
    }

    @Override
    public Object getValue(WebRequest request, HeaderValue annotation, Method method, Parameter parameter) {
        Class<?> type = parameter.getType();

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
