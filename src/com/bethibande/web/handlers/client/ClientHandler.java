package com.bethibande.web.handlers.client;

import com.bethibande.web.JWebClient;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.context.ClientContext;
import com.bethibande.web.context.IContext;
import com.bethibande.web.processors.MethodInvocationHandler;
import com.bethibande.web.processors.client.ClientParameterProcessor;
import com.bethibande.web.processors.client.ClientProcessorMappings;
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
import java.util.HashMap;

import static com.bethibande.web.logging.ConsoleColors.*;

public class ClientHandler implements InvocationHandler {

    private final JWebClient owner;
    private final Class<?> type;
    private final HashMap<Method, ClientProcessorMappings> parameterMappings = new HashMap<>();

    public ClientHandler(final JWebClient owner, final Class<?> type) {
        this.owner = owner;
        this.type = type;

        generateParameterMappings();
    }

    private void generateParameterMappings() {
        for(Method method : type.getDeclaredMethods()) {
            if(!method.isAnnotationPresent(URI.class)) continue;

            parameterMappings.put(method, ClientProcessorMappings.of(method, owner));
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws IOException {
        if(!method.isAnnotationPresent(URI.class)) return null;

        final Request request = new Request(null, new Headers(), null, null);

        for(MethodInvocationHandler handler : owner.getMethodInvocationHandlers()) {
            handler.beforeInvocation(method, request, owner);
        }

        final IContext context = new ClientContext(owner, request);
        final Parameter[] parameters = method.getParameters();
        final ClientParameterProcessor[] processors = parameterMappings.get(method).processors();

        for(int i = 0; i < parameters.length; i++) {
            if(processors[i] == null) continue;
            processors[i].function().apply(context, parameters[i], args[i]);
        }

        owner.getLogger().fine(String.format(
                "Sending request %s %s",
                annotate(request.method().toString(), CYAN + BOLD),
                annotate(request.uri().toString(), RESET + GREEN)
        ));

        final RequestResponse response = owner.sendRequest(request);
        final InputStreamWrapper in = (InputStreamWrapper) response.getContentData();

        ResponseReader reader = owner.getReaders().get(method.getReturnType());
        if(reader == null) reader = owner.getReaders().get(Object.class);

        request.setResponseData(reader.read(in, response, method.getGenericReturnType()));

        for(MethodInvocationHandler handler : owner.getMethodInvocationHandlers()) {
            handler.afterInvocation(method, request, owner);
        }

        return request.responseData();
    }
}
