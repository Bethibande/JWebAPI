package com.bethibande.web.processors.impl;

import com.bethibande.web.beans.GlobalBean;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.FilteredParameterProcessor;
import com.bethibande.web.processors.ParameterFilter;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class GlobalBeanParameterProcessor extends FilteredParameterProcessor {

    public GlobalBeanParameterProcessor() {
        super(ParameterFilter.typeAssignableFilter(GlobalBean.class));
    }

    @Override
    public Object process(ServerContext context, Executable executable, Parameter parameter) {
        return context.api().getGlobalBean((Class<? extends GlobalBean>) parameter.getType());
    }

}
