package com.bethibande.web.examples.test.annotations;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.examples.test.Message;
import com.bethibande.web.examples.test.SecuredContext;
import com.bethibande.web.processors.AnnotatedInvocationHandler;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.Request;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Method;

public class SecuredAnnotationHandler extends AnnotatedInvocationHandler<SecuredEntry> {

    public SecuredAnnotationHandler() {
        super(SecuredEntry.class);
    }

    @Override
    public void beforeInvocation(Method method, SecuredEntry annotation, Request _request, JWebAPI api) {
        if(!(_request instanceof WebRequest request)) throw new RuntimeException("Not supported");

        ServerContext context = LocalServerContext.getContext();
        if(!(context instanceof SecuredContext securedContext)) return;

        if(securedContext.getPermissions().can(annotation.permission().getId(), annotation.access().toByte())) return;

        request.setResponse(new RequestResponse()
                .withStatusCode(403)
                .withContentData(Message.MessageType.ACCESS_DENIED.toMessage())
        );

        request.setFinished(true);
    }

    @Override
    public void afterInvocation(Method method, SecuredEntry annotation, Request request, JWebAPI server) { }
}
