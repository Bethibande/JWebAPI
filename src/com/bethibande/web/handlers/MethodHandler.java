package com.bethibande.web.handlers;

import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.types.ProcessorMappings;
import com.bethibande.web.response.RequestResponse;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public abstract class MethodHandler {

    private final Method method;
    private final ProcessorMappings mappings;

    public MethodHandler(Method method, ProcessorMappings mappings) {
        this.method = method;
        this.mappings = mappings;

        method.setAccessible(true);
    }

    public Method getMethod() {
        return method;
    }

    protected void prepare(ServerContext context) {
        final Parameter[] parameters = method.getParameters();
        final ParameterProcessor[] processors = mappings.getProcessors();
        final Object[] values = new Object[parameters.length];

        for(int i = 0; i < parameters.length; i++) {
            values[i] = processors[i].process(context, method, parameters[i]);
        }

        context.request().setMethodInvocationParameters(values);
        context.request().setFinished(false);
    }

    public abstract RequestResponse invoke(ServerContext context);

}
