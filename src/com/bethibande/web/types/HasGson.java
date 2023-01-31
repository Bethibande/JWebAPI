package com.bethibande.web.types;

import com.google.gson.Gson;

public interface HasGson {

    void setGson(final Gson gson);
    Gson getGson();

}
