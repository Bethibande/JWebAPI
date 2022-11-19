package com.bethibande.web.processors.impl;

import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.types.WebRequest;

import java.io.InputStream;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class InputStreamParameterProcessor implements ParameterProcessor {

    @Override
    public void process(ServerContext context, int parameterIndex, Executable executable, Parameter parameter) {
        if(!InputStream.class.isAssignableFrom(parameter.getType())) return;
        final WebRequest request = context.request();
        request.setParameter(parameterIndex, request.getExchange().getRequestBody());
    }
}
