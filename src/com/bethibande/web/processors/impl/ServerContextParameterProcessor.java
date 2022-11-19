package com.bethibande.web.processors.impl;

import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.ParameterProcessor;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class ServerContextParameterProcessor implements ParameterProcessor {

    @Override
    public void process(ServerContext context, int parameterIndex, Executable executable, Parameter parameter) {
        if(ServerContext.class.isAssignableFrom(parameter.getType())) {
            context.request().setParameter(parameterIndex, LocalServerContext.getContext());
        }
    }
}
