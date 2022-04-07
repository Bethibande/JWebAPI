package com.bethibande.web.struct;

import com.sun.net.httpserver.Headers;
import com.bethibande.web.handlers.MethodHandle;

import java.net.InetSocketAddress;
import java.util.HashMap;

public interface IAccessManager {

    boolean canAccess(InetSocketAddress user, String uri, String method, Headers header, MethodHandle handle, HashMap<String, String> query);

}
