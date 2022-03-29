package de.bethibande.web.examples;
import de.bethibande.web.URI;
import de.bethibande.web.annotations.QueryField;
import de.bethibande.web.response.StreamResponse;

public interface Client {

    @URI("/api/test")
    TestHandler.Message getMessage(@QueryField(value = "name", def = "World") String name);

    @URI("/api/")
    TestHandler.Message test();

    @URI("/api/file")
    StreamResponse getFile();

}
