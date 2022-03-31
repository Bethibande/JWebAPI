package de.bethibande.web.tcp;

import com.sun.net.httpserver.HttpServer;
import de.bethibande.web.JWebServer;
import de.bethibande.web.RequestProcessor;
import de.bethibande.web.StandardRequestProcessor;
import de.bethibande.web.handlers.HandlerManager;
import de.bethibande.web.handlers.WebHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TCPServer implements JWebServer {

    private int port = 0;
    private int backlog = 0;
    private int bufferSize = 1024;
    private Charset charset = StandardCharsets.UTF_8;

    private HttpServer server;
    private HandlerManager handlerManager;
    private RequestProcessor processor;

    @Override
    public JWebServer port(int port) {
        this.port = port;
        this.handlerManager = new HandlerManager();
        this.processor = new StandardRequestProcessor(this);
        return this;
    }

    @Override
    public void registerHandler(Class<? extends WebHandler> handler) {
        this.handlerManager.registerHandler(handler);
    }

    public HandlerManager getHandlerManager() {
        return this.handlerManager;
    }

    public RequestProcessor getProcessor() {
        return this.processor;
    }

    public TCPServer backlog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    @Override
    public JWebServer bufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    @Override
    public JWebServer charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    @Override
    public Charset getCharset() {
        return this.charset;
    }

    @Override
    public int getPort() {
        return this.server != null ? server.getAddress().getPort(): this.port;
    }

    @Override
    public int getBufferSize() {
        return this.bufferSize;
    }

    @Override
    public void start() {
        if(isAlive()) stop();
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", this.port), this.backlog);
            server.createContext("/", new ServerHandler(this));
            server.setExecutor(null);
            server.start();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if(!isAlive()) return;
        this.server.stop(0);
        this.server = null;
    }

    @Override
    public boolean isAlive() {
        return this.server != null;
    }
}
