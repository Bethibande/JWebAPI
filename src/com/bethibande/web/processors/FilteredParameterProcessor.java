package com.bethibande.web.processors;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public abstract class FilteredParameterProcessor implements ParameterProcessor {

    private final ParameterFilter filter;

    public FilteredParameterProcessor(ParameterFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean applies(Executable executable, Parameter parameter) {
        return filter.filter(executable, parameter);
    }
}
