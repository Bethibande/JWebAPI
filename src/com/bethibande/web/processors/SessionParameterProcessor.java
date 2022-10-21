package com.bethibande.web.processors;

import com.bethibande.web.LocalServerContext;
import com.bethibande.web.WebRequest;
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
