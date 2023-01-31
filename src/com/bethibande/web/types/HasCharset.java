package com.bethibande.web.types;

import java.nio.charset.Charset;

public interface HasCharset {

    void setCharset(final Charset charset);
    Charset getCharset();

}
