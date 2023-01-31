package com.bethibande.web.types;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.response.RequestResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public abstract class ResponseReader {

    protected final JWebAPI owner;

    public ResponseReader(final JWebAPI owner) {
        this.owner = owner;
    }

    public abstract Object read(final InputStream in,
                           final RequestResponse response,
                           final Type returnType) throws IOException;

}
