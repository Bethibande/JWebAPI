package com.bethibande.web.processors.impl;

import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.FilteredParameterProcessor;
import com.bethibande.web.processors.ParameterFilter;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class ServerContextParameterProcessor extends FilteredParameterProcessor {

    public ServerContextParameterProcessor() {
        super(ParameterFilter.typeAssignableFilter(ServerContext.class));
    }

    @Override
    public Object process(ServerContext context, Executable executable, Parameter parameter) {
        return LocalServerContext.getContext();
    }
}
