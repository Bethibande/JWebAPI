package com.bethibande.web.processors.impl;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.JWebServer;
import com.bethibande.web.beans.BeanManager;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.MethodInvocationHandlerAdapter;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.types.Request;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Method;

public class BeanHandler extends MethodInvocationHandlerAdapter {

    @Override
    public void afterInvocation(Method method, Request request, JWebAPI api) {
        if(!(api instanceof JWebServer server)) {
            throw new RuntimeException("Beans are not supported by client.");
        }

        ServerContext context = LocalServerContext.getContext();
        Session session = context.session();
        if(!session.getMeta().hasMeta("localBeanManager")) return;

        session.getMeta().getAsType("localBeanManager", BeanManager.class).storeActiveBeans();
    }
}
