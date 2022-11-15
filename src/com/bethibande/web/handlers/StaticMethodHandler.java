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
        Method method = getMethod();

        for(MethodInvocationHandler handler : server.getMethodInvocationHandlers()) {
            if(request.isFinished()) break;

            handler.beforeInvocation(method, request, server);
        }
        if(request.isFinished()) return request.getResponse();

        super.prepare(request);

        try {
            Object value = getMethod().invoke(null, request.getMethodInvocationParameters());
            request.getResponse().setContentData(value);
        } catch(IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        for(MethodInvocationHandler handler : server.getMethodInvocationHandlers()) {
            handler.afterInvocation(method, request, server);
        }

        return request.getResponse();
    }
}
