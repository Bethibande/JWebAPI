package com.bethibande.web.processors.impl;

import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.FilteredParameterProcessor;
import com.bethibande.web.processors.ParameterFilter;
import com.bethibande.web.types.WebRequest;

import java.io.InputStream;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class InputStreamParameterProcessor extends FilteredParameterProcessor {

    public InputStreamParameterProcessor() {
        super(ParameterFilter.typeFilter(InputStream.class));
    }

    @Override
    public Object process(ServerContext context, Executable executable, Parameter parameter) {
        final WebRequest request = context.request();
        return request.getExchange().getRequestBody();
    }
}
