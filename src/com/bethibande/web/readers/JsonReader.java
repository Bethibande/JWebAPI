package com.bethibande.web.readers;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.ResponseReader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public class JsonReader extends ResponseReader {

    public JsonReader(final JWebAPI owner) {
        super(owner);
    }

    @Override
    public Object read(final InputStream in, final RequestResponse response, final Type returnType) throws IOException {
        final byte[] data = in.readNBytes((int) response.getContentLength());
        final String json = new String(data, owner.getCharset());

        response.disconnect();

        return owner.getGson().fromJson(json, returnType);
    }
}
