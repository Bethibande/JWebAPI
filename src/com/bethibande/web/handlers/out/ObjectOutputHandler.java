package com.bethibande.web.handlers.out;

import com.bethibande.web.types.WebRequest;
import com.bethibande.web.response.RequestResponse;
import com.google.gson.Gson;

public class ObjectOutputHandler implements OutputHandler<Object> {

    public static final String CONTENT_TYPE = "text/json";

    @Override
    public Class<Object> getType() {
        return Object.class;
    }

    @Override
    public void update(Object value, WebRequest request) {
        byte[] data = new Gson().toJson(value).getBytes(request.getResponse().getCharset());

        RequestResponse response = request.getResponse();
        if(response.getContentType() == null) response.setContentType(ObjectOutputHandler.CONTENT_TYPE);
        response.setContentLength(data.length);
        response.setContentData(data);
    }
}
