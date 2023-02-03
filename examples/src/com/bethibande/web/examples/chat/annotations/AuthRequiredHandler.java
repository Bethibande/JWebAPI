package com.bethibande.web.examples.chat.annotations;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.examples.chat.context.ChatContext;
import com.bethibande.web.processors.AnnotatedInvocationHandler;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.Request;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.util.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.lang.reflect.Method;

public class AuthRequiredHandler extends AnnotatedInvocationHandler<AuthRequired> {

    public AuthRequiredHandler() {
        super(AuthRequired.class);
    }

    /**
     * Needed to prevent name spoofing and preventing people from reading messages without logging in
     */
    @Override
    public void beforeInvocation(final Method method, final AuthRequired annotation, final Request _request, final JWebAPI server) {
        final ChatContext context = (ChatContext) LocalServerContext.getContext();
        final WebRequest request = (WebRequest) _request;

        if(annotation.simple() && context.hasAuth()) return;

        Gson gson = request.getServer().getGson();

        String json = IOUtils.readString(
                request.getExchange().getRequestBody(),
                request.getContentLength(),
                context.metadata().getCharset(),
                context.metadata().getBufferSize()
        );

        final JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        context.metadata().set(
                "JsonData",
                jsonObject
        );

        final String name = jsonObject.get("name").getAsString();
        if(context.ownsName(name)) return;

        request.setResponse(RequestResponse.build().withStatusCode(403));
        request.setFinished(true);
    }
}
