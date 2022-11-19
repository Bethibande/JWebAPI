package com.bethibande.web.processors;


import com.bethibande.web.context.ServerContext;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface ParameterProcessor {

    void process(ServerContext context, int parameterIndex, Executable executable, Parameter parameter);

}
