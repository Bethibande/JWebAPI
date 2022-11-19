package com.bethibande.web.processors;

import com.bethibande.web.context.ServerContext;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public abstract class FilteredParameterProcessor implements ParameterProcessor {

    private final ParameterFilter filter;

    public FilteredParameterProcessor(ParameterFilter filter) {
        this.filter = filter;
    }

    public abstract void accept(ServerContext context, int index, Executable executable, Parameter parameter);

    @Override
    public void process(ServerContext context, int parameterIndex, Executable executable, Parameter parameter) {
        if(!filter.filter(context, executable, parameter)) return;
        accept(context, parameterIndex, executable, parameter);
    }
}
