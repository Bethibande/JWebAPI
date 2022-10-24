package com.bethibande.web.handlers.out;

import com.bethibande.web.types.WebRequest;
import com.bethibande.web.response.RequestResponse;

public class RequestResponseOutputHandler implements OutputHandler<RequestResponse> {

    @Override
    public Class<RequestResponse> getType() {
        return RequestResponse.class;
    }

    @Override
    public void update(RequestResponse value, WebRequest request) {
        request.setResponse(value);

        if(value.getContentData() != null && value.getContentData().getClass().equals(RequestResponse.class)) throw new RuntimeException("Not allowed.");
    }
}
