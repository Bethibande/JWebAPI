package com.bethibande.web.context;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.types.Request;

public interface IContext {

    JWebAPI api();
    Request request();

}
