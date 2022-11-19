package com.bethibande.web.processors;


import com.bethibande.web.context.ServerContext;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public interface ParameterProcessor {

    void process(ServerContext context, int parameterIndex, Executable executable, Parameter parameter);

}
