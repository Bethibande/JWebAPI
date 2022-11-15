package com.bethibande.web.handlers;

import com.bethibande.web.JWebServer;
import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.types.WebRequest;
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

    protected void prepare(WebRequest request) {
        JWebServer server = request.getServer();
        Parameter[] parameters = method.getParameters();

        for(int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            for(ParameterProcessor processor : server.getProcessors()) {
                processor.process(request, i, method, parameter);
            }
        }

        request.setFinished(false);
    }

    public abstract RequestResponse invoke(WebRequest request);

}
