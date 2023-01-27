package com.bethibande.web.types;

import com.sun.net.httpserver.Headers;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

public record Request(URL url,
                      Headers headers,
                      RequestMethod method,
                      @Nullable RequestWriter writer) {

}
