package com.bethibande.web.examples;
import com.bethibande.web.annotations.FieldName;
import com.bethibande.web.annotations.JsonMappings;
import com.bethibande.web.annotations.QueryField;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.response.StreamResponse;

public interface Client {

    @URI("/api/test")
    TestHandler.Message getMessage(@QueryField(value = "name", def = "World") String name);

    @URI("/api/")
    TestHandler.Message test();

    @URI("/api/file")
    StreamResponse getFile();

    /**
     * this will create a post request with a json object as the request body
     * the json obj will be formed automatically, in this case it will be
     * {"username":"usernameValue","password":"passwordValue"}
     * please note, the @FieldName annotations are not necessary if you enable the javac -parameters option,
     * if you do the @FieldName annotations will be ignored and the json field names will be equal to the java parameter names
     */
    @URI("/api/login")
    @JsonMappings
    boolean login(@FieldName("username") String username, @FieldName("password") String password);

}
