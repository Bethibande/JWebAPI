package com.bethibande.web.context;

import com.bethibande.web.types.MetaData;

import java.nio.charset.Charset;

public class ContextMeta extends MetaData {

    public int getBufferSize() {
        return getInteger("bufferSize");
    }

    public void setBufferSize(int bufferSize) {
        set("bufferSize", bufferSize);
    }

    public Charset getCharset() {
        return getAsType("charset", Charset.class);
    }

    public void setCharset(Charset charset) {
        set("charset", charset);
    }

}
