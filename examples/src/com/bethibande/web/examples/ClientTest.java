package com.bethibande.web.examples;

import com.bethibande.web.JWebClient;
import com.bethibande.web.examples.client.ExampleRepository;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.Request;
import com.bethibande.web.types.RequestMethod;

public class ClientTest {

    public static void main(final String[] args) {
        final JWebClient client = new JWebClient()
                .withBaseUrl("http://127.0.0.1:5544");

        /*final Message message1 = (Message) client.getJson(
                Request.ofString("http://127.0.0.1:5544/count", RequestMethod.GET),
                Message.class
        ).getContentData();

        System.out.println(message1.id() + " " + message1.message());*/

        final ExampleRepository repository = client.withRepository(ExampleRepository.class);
        final Message message2 = repository.queryTest("test text", true);

        System.out.println(message2.id() + " " + message2.message());

        /*final long start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++) {
            repository.count();
        }
        System.out.println(System.currentTimeMillis() - start);*/

        /*final long start = System.currentTimeMillis();
        for(int i = 0; i < 10_000; i++) {
            final RequestResponse response = client.getString(Request.ofString("http://127.0.0.1:5544/count", RequestMethod.GET));
            if(i % 100 == 0) System.out.println((int)((i+1) / 10_000.0 * 100) + "%");
        }
        final long time = System.currentTimeMillis() - start;
        System.out.println("Took: " + time + " ms");*/

        client.destroy();
    }

}
