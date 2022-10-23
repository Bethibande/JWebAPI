package com.bethibande.web.processors.impl;

import com.bethibande.web.JWebServer;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.processors.AnnotatedInvocationHandler;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.RequestMethod;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Method;

/**
 * Used for handling stuff like the RequestMethod parameter of the {@link URI} annotation
 */
public class URIAnnotationProcessor extends AnnotatedInvocationHandler<URI> {

    public URIAnnotationProcessor() {
        super(URI.class);
    }

    @Override
    public void beforeInvocation(Method _method, URI annotation, WebRequest request, JWebServer server) {
        if(annotation.methods() == null || annotation.methods().length == 0) return;

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

    @Override
    public void afterInvocation(Method method, URI annotation, WebRequest request, JWebServer server) { }
}
