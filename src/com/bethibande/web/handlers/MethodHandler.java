package com.bethibande.web.handlers;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.response.RequestResponse;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public abstract class MethodHandler {

    private final Method method;

    public MethodHandler(Method method) {
        this.method = method;

        method.setAccessible(true);
    }

    public Method getMethod() {
        return method;
    }

    protected void prepare(ServerContext context) {
        final JWebServer server = context.server();
        final Parameter[] parameters = method.getParameters();

        for(int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            for(ParameterProcessor processor : server.getProcessors()) {
                processor.process(context, i, method, parameter);
            }
        }

        context.request().setFinished(false);
    }

    public abstract RequestResponse invoke(ServerContext context);

}
