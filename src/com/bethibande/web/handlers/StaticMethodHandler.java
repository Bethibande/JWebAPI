package com.bethibande.web.handlers;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.processors.MethodInvocationHandler;
import com.bethibande.web.response.RequestResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StaticMethodHandler extends MethodHandler {

    public StaticMethodHandler(Method method) {
        super(method);
    }

    @Override
    public RequestResponse invoke(WebRequest request) {
        JWebServer server = request.getServer();

        for(MethodInvocationHandler handler : server.getMethodInvocationHandlers()) {
            if(request.isFinished()) break;

            handler.beforeInvocation(getMethod(), request, server);
        }
        if(request.isFinished()) return request.getResponse();

        super.prepare(request);

        try {
            Object value = getMethod().invoke(null, request.getMethodInvocationParameters());
            if(value != null) request.getResponse().setContentData(value);
        } catch(IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        request.setFinished(false);

        for(MethodInvocationHandler handler : server.getMethodInvocationHandlers()) {
            if(request.isFinished()) break;

            handler.afterInvocation(getMethod(), request, server);
        }

        return request.getResponse();
    }
}
