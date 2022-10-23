package com.bethibande.web.processors.impl;

import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.sessions.Session;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class SessionParameterProcessor implements ParameterProcessor {

    @Override
    public void process(WebRequest request, int parameterIndex, Method method, Parameter parameter) {
        if(Session.class.isAssignableFrom(parameter.getType())) {
            request.setParameter(parameterIndex, LocalServerContext.getSession());
        }
    }
}
