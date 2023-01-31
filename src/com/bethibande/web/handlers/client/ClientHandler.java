package com.bethibande.web.handlers.client;

import com.bethibande.web.JWebClient;
import com.bethibande.web.context.ClientContext;
import com.bethibande.web.context.IContext;
import com.bethibande.web.processors.MethodInvocationHandler;
import com.bethibande.web.processors.client.ClientParameterProcessor;
import com.bethibande.web.response.InputStreamWrapper;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.Request;
import com.bethibande.web.types.ResponseReader;
import com.sun.net.httpserver.Headers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static com.bethibande.web.logging.ConsoleColors.*;

public class ClientHandler implements InvocationHandler {

    private final JWebClient owner;

    public ClientHandler(final JWebClient owner) {
        this.owner = owner;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws IOException {
        final Request request = new Request(null, new Headers(), null, null);

        for(MethodInvocationHandler handler : owner.getMethodInvocationHandlers()) {
            handler.beforeInvocation(method, request, owner);
        }

        owner.getLogger().fine(String.format(
                "Sending request %s %s",
                annotate(request.method().toString(), CYAN + BOLD),
                annotate(request.uri().toString(), RESET + GREEN)
        ));

        final IContext context = new ClientContext(owner, request);

        int index = 0;
        for(Parameter parameter : method.getParameters()) {
            for(ClientParameterProcessor processor : owner.getProcessors()) {
                if(!processor.predicate().apply(parameter)) continue;
                processor.function().apply(context, parameter, args[index]);
            }

            index++;
        }

        final RequestResponse response = owner.sendRequest(request);
        final InputStream in = ((InputStreamWrapper) response.getContentData()).getStream();

        ResponseReader reader = owner.getReaders().get(method.getReturnType());
        if(reader == null) reader = owner.getReaders().get(Object.class);

        request.setResponseData(reader.read(in, response, method.getGenericReturnType()));

        for(MethodInvocationHandler handler : owner.getMethodInvocationHandlers()) {
            handler.afterInvocation(method, request, owner);
        }

        return request.responseData();
    }
}
