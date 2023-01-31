package com.bethibande.web.context;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.types.Request;

public record ClientContext(JWebAPI api, Request request) implements IContext {

}
