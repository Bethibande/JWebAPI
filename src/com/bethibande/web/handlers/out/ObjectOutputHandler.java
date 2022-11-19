package com.bethibande.web.handlers.out;

import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.response.RequestResponse;

public class ObjectOutputHandler implements OutputHandler<Object> {

    public static final String CONTENT_TYPE = "text/json";

    @Override
    public Class<Object> getType() {
        return Object.class;
    }

    @Override
    public void update(Object value, WebRequest request) {
        ServerContext context = LocalServerContext.getContext();
        if(context == null) throw new RuntimeException("Cannot write object without context.");

        byte[] data = request.getServer().getGson().toJson(value).getBytes(context.metadata().getCharset());

        RequestResponse response = request.getResponse();
        if(response.getContentType() == null) response.setContentType(ObjectOutputHandler.CONTENT_TYPE);
        response.setContentLength(data.length);
        response.setContentData(data);
    }
}
