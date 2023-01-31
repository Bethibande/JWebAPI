package com.bethibande.web.processors.impl;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.JWebClient;
import com.bethibande.web.JWebServer;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.processors.AnnotatedInvocationHandler;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.Request;
import com.bethibande.web.types.RequestMethod;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Used for handling stuff like the RequestMethod parameter of the {@link URI} annotation
 */
public class URIAnnotationHandler extends AnnotatedInvocationHandler<URI> {

    public URIAnnotationHandler() {
        super(URI.class);
    }

    private void handleServer(final WebRequest request, final URI annotation) {
        RequestMethod[] methods = annotation.methods();
        String method = request.getExchange().getRequestMethod();
        boolean validMethod = false;

        for(RequestMethod m : methods) {
            if(m.toString().equals(method)) validMethod = true;
        }

        if(validMethod) return;

        request.setResponse(new RequestResponse().withStatusCode(405));
        request.setFinished(true);
    }

    private void handleClient(final JWebClient owner, final Request request, final URI annotation) {
        final RequestMethod method = annotation.methods().length > 0 ? annotation.methods()[0]: RequestMethod.GET;
        final java.net.URI uri = java.net.URI.create(annotation.value());

        request.setUri(uri);
        request.setMethod(method);
    }

    @Override
    public void beforeInvocation(Method _method, URI annotation, Request request, JWebAPI api) {
        if(annotation.methods() == null || annotation.methods().length == 0) return;

        if(api instanceof JWebServer) handleServer((WebRequest) request, annotation);
        if(api instanceof JWebClient) handleClient((JWebClient) api, request, annotation);
    }

    @Override
    public void afterInvocation(Method method, URI annotation, Request request, JWebAPI api) { }
}
