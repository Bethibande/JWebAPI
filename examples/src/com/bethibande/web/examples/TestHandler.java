package com.bethibande.web.examples;

import com.bethibande.web.annotations.CachedRequest;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.response.RequestResponse;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class TestHandler {

    @URI("/")
    public Object helloWorld() {
        return Message.MessageType.HELLO_WORLD.toMessage();
    }

    @URI("/test")
    @CachedRequest(cacheTime = 60000L, global = true)
    public Object test() {
        return RequestResponse.build()
                .withStatusCode(202)
                .withCharset(StandardCharsets.UTF_8)
                .withHeader("Date", new Date())
                .withContentData(Message.MessageType.TEST.toMessage());
    }

    @URI("/redirect")
    public Object redirect() {
        return RequestResponse.build()
                .withStatusCode(301)
                .withLocation("/test");
    }

}
