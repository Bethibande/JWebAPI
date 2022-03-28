package de.bethibande.web;

import com.sun.net.httpserver.Headers;
import de.bethibande.web.handlers.MethodHandle;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;

public interface RequestProcessor {

    Object processRequest(InetSocketAddress sender, String uri, String method, InputStream in, Object instance, MethodHandle handle, HashMap<String, String> query, Headers headers) throws IOException;

}
