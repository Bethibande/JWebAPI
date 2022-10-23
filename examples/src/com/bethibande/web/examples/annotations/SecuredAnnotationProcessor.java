package com.bethibande.web.examples.annotations;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.examples.Message;
import com.bethibande.web.examples.SecuredContext;
import com.bethibande.web.processors.AnnotatedInvocationHandler;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Method;

public class SecuredAnnotationProcessor extends AnnotatedInvocationHandler<SecuredEntry> {

    public SecuredAnnotationProcessor() {
        super(SecuredEntry.class);
    }

    @Override
    public void beforeInvocation(Method method, SecuredEntry annotation, WebRequest request, JWebServer server) {
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
    public void afterInvocation(Method method, SecuredEntry annotation, WebRequest request, JWebServer server) { }
}
