package com.bethibande.web.processors;


import com.bethibande.web.context.ServerContext;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public interface ParameterProcessor {

    Object process(ServerContext context, Executable executable, Parameter parameter);

    /**
     * Return true if the processor should be applied for the parameter of the given executable
     * @param executable the executable containing the parameter
     * @param parameter the parameter to check
     * @return true if the processor should apply to the given parameter
     */
    boolean applies(Executable executable, Parameter parameter);

}
