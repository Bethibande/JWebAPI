package com.bethibande.web.types;

import com.bethibande.web.JWebServer;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public record ServerInterface(JWebServer owner, InetSocketAddress address, HttpServer server) {

}
