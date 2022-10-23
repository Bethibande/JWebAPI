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

        for(int i = 0; i < method.getParameterCount(); i++) {
            Parameter parameter = method.getParameters()[i];

            for(ParameterProcessor processor : server.getProcessors()) {
                processor.process(request, i, method, parameter);
            }

            if(request.isFinished()) break;
        }

        request.setFinished(false);
    }

    public abstract RequestResponse invoke(WebRequest request);

}
