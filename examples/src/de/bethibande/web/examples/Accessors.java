package de.bethibande.web.examples;

import com.sun.net.httpserver.Headers;
import de.bethibande.web.handlers.MethodHandle;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class Accessors {

    // this blocks access/requests from any device that isn't coming from the same device this code is running on, only 127.0.0.1/localhost calls allowed
    public static boolean localOnly(InetSocketAddress user, String uri, String method, Headers header, MethodHandle handle, HashMap<String, String> query) {
        return user.getAddress().toString().equalsIgnoreCase("/127.0.0.1");
    }

}
