package com.bethibande.web.readers;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.response.InputStreamWrapper;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.ResponseReader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public class StreamReader extends ResponseReader {

    public StreamReader(final JWebAPI owner) {
        super(owner);
    }

    @Override
    public Object read(final InputStreamWrapper in, final RequestResponse response, final Type returnType) throws IOException {
        if(InputStream.class.getTypeName().equals(returnType.getTypeName())) {
            return in.getStream();
        }

        return in;
    }
}
