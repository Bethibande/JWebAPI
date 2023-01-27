package com.bethibande.web.examples;

import com.bethibande.web.JWebClient;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.Request;
import com.bethibande.web.types.RequestMethod;

public class ClientTest {

    public static void main(final String[] args) {
        final JWebClient client = new JWebClient();

        final Message message = (Message) client.getJson(
                Request.ofString("http://127.0.0.1:5544/count", RequestMethod.GET),
                Message.class
        ).getContentData();

        System.out.println(message.id() + " " + message.message());

        /*final long start = System.currentTimeMillis();
        for(int i = 0; i < 10_000; i++) {
            final RequestResponse response = client.getString(Request.ofString("http://127.0.0.1:5544/count", RequestMethod.GET));
            //if(!response.getContentData().equals("{\"id\":200,\"message\":\"GOOD\"}")) throw new RuntimeException("Error: " + response.getContentData());
            if(i % 100 == 0) System.out.println((int)((i+1) / 10_000.0 * 100) + "%");
        }
        final long time = System.currentTimeMillis() - start;
        System.out.println("Took: " + time + " ms");*/
    }

}
