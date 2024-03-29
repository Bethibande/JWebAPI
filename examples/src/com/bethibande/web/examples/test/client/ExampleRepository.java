package com.bethibande.web.examples.test.client;

import com.bethibande.web.annotations.PostData;
import com.bethibande.web.annotations.QueryField;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.examples.test.Message;
import com.bethibande.web.types.RequestMethod;

public interface ExampleRepository {

    @URI(value = "/count", methods = RequestMethod.GET)
    Message count();

    @URI(value = "http://127.0.0.1:5544/localCache", methods = RequestMethod.GET)
    Message localCache();

    @URI(value = "/postMessage", methods = RequestMethod.POST)
    Message postMessage(final @PostData Message message);

    @URI(value = "/textMessage", methods = RequestMethod.GET)
    Message queryTest(final @QueryField("text") String text, final @QueryField("json") boolean json);

}
