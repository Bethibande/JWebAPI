package com.bethibande.web.processors.impl;

import com.bethibande.web.beans.Bean;
import com.bethibande.web.beans.BeanManager;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.types.MetaData;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class BeanParameterProcessor implements ParameterProcessor {

    @Override
    public void process(WebRequest request, int parameterIndex, Executable method, Parameter parameter) {
        if(Bean.class.isAssignableFrom(parameter.getType())) {
            ServerContext context = LocalServerContext.getContext();
            Session session = context.session();
            MetaData meta = session.getMeta();

            if(!meta.hasMeta("localBeanManager")) {
                meta.set("localBeanManager", new BeanManager());
            }

            BeanManager beanManager = meta.getAsType("localBeanManager", BeanManager.class);
            Bean bean = beanManager.activeBean((Class<Bean>) parameter.getType(), context);

            request.setParameter(parameterIndex, bean);
        }
    }
}
