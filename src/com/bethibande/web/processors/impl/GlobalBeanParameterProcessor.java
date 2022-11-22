package com.bethibande.web.processors.impl;

import com.bethibande.web.beans.GlobalBean;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.ParameterProcessor;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class GlobalBeanParameterProcessor implements ParameterProcessor {

    @Override
    public void process(ServerContext context, int parameterIndex, Executable executable, Parameter parameter) {
        if(GlobalBean.class.isAssignableFrom(parameter.getType())) {
            context.request().setParameter(parameterIndex, context.server().getGlobalBean((Class<? extends GlobalBean>) parameter.getType()));
        }
    }
}
