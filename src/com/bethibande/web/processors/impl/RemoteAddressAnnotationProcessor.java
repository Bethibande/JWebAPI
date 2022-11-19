package com.bethibande.web.processors.impl;

import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.AnnotationProcessor;
import com.bethibande.web.annotations.RemoteAddress;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class RemoteAddressAnnotationProcessor extends AnnotationProcessor<RemoteAddress> {

    public RemoteAddressAnnotationProcessor() {
        super(RemoteAddress.class, true);
    }

    @Override
    public Object accept(ServerContext context, RemoteAddress annotation, Executable executable, Parameter parameter) {
        return context.request().getExchange().getRemoteAddress();
    }
}
