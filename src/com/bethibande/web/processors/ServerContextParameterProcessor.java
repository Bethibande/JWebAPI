package com.bethibande.web.processors;

import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.WebRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ServerContextParameterProcessor implements ParameterProcessor {

    @Override
    public void process(WebRequest request, int parameterIndex, Method method, Parameter parameter) {
        if(ServerContext.class.isAssignableFrom(parameter.getType())) {
            request.setParameter(parameterIndex, LocalServerContext.getContext());
        }
    }
}
