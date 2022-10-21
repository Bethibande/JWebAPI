package com.bethibande.web;

import com.bethibande.web.sessions.Session;
import com.sun.net.httpserver.HttpExchange;

public record ServerContext(
    JWebServer server,
    Session session,
    HttpExchange exchange,
    WebRequest request
) { }
