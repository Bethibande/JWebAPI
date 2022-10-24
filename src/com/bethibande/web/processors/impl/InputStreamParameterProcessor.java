package com.bethibande.web.processors.impl;

import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.types.WebRequest;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class InputStreamParameterProcessor implements ParameterProcessor {

    @Override
    public void process(WebRequest request, int parameterIndex, Method method, Parameter parameter) {
        if(!InputStream.class.isAssignableFrom(parameter.getType())) return;

        request.setParameter(parameterIndex, request.getExchange().getRequestBody());
    }
}
