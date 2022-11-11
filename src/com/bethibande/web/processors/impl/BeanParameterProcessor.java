package com.bethibande.web.processors.impl;

import com.bethibande.web.beans.Bean;
import com.bethibande.web.beans.BeanManager;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class BeanParameterProcessor implements ParameterProcessor {

    @Override
    public void process(WebRequest request, int parameterIndex, Executable method, Parameter parameter) {
        if(Bean.class.isAssignableFrom(parameter.getType())) {
            ServerContext context = LocalServerContext.getContext();
            Session session = context.session();

            if(!session.getMeta().hasMeta("localBeanManager")) {
                session.getMeta().set("localBeanManager", new BeanManager());
            }

            BeanManager beanManager = session.getMeta().getAsType("localBeanManager", BeanManager.class);
            Bean bean = beanManager.getBean((Class<Bean>) parameter.getType(), context);
            beanManager.activate(bean);

            request.setParameter(parameterIndex, bean);
        }
    }
}
