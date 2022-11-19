package com.bethibande.web.processors.impl;

import com.bethibande.web.beans.Bean;
import com.bethibande.web.beans.BeanManager;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.FilteredParameterProcessor;
import com.bethibande.web.processors.ParameterFilter;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.types.MetaData;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class BeanParameterProcessor extends FilteredParameterProcessor {

    public BeanParameterProcessor() {
        super(ParameterFilter.requestFilter());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void accept(ServerContext context, int parameterIndex, Executable executable, Parameter parameter) {
        if(Bean.class.isAssignableFrom(parameter.getType())) {
            Session session = context.session();
            MetaData meta = session.getMeta();

            if(!meta.hasMeta("localBeanManager")) {
                meta.set("localBeanManager", new BeanManager());
            }

            BeanManager beanManager = meta.getAsType("localBeanManager", BeanManager.class);
            Bean bean = beanManager.activeBean((Class<Bean>) parameter.getType(), context);

            context.request().setParameter(parameterIndex, bean);
        }
    }
}
