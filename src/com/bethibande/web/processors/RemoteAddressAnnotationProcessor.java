package com.bethibande.web.processors;

import com.bethibande.web.WebRequest;
import com.bethibande.web.annotations.RemoteAddress;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class RemoteAddressAnnotationProcessor extends AnnotationProcessor<RemoteAddress> {

    public RemoteAddressAnnotationProcessor() {
        super(RemoteAddress.class);
    }

    @Override
    public Object getValue(WebRequest request, RemoteAddress annotation, Method method, Parameter parameter) {
        return request.getExchange().getRemoteAddress();
    }
}
