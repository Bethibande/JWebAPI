package com.bethibande.web;

import com.bethibande.web.handlers.HandlerManager;
import com.bethibande.web.handlers.WebHandler;
import com.bethibande.web.tcp.TCPServer;

import java.nio.charset.Charset;

// TODO: ssl encryption
// TODO: field annotations: URIField(field) = get fields from uri, define uri fields within the uri annotation uri = (/test/{field-name})
public interface JWebServer {

    /**
     * Set the port of your server, only called before starting the server
     * @param port the port your server will be bound to
     * @return your server instance
     */
    JWebServer port(int port);

    /**
     * Change the buffer size of your server, the default value is 1024
     * @param bufferSize the buffer size
     * @return your server instance
     */
    JWebServer bufferSize(int bufferSize);

    /**
     * Change the charset used by the server, default StandardCharsets.UTF_8
     * @param charset the new charset to use
     * @return your server instance
     */
    JWebServer charset(Charset charset);

    /**
     * Get the server charset
     * @return charset, default StandardCharsets.UTF_8
     */
    Charset getCharset();

    /**
     * Get the port your server is currently bound to
     * @return the server port
     */
    int getPort();

    /**
     * Get the buffer size of your server, change using JWebServer.bufferSize();
     * @return the current buffer size
     */
    int getBufferSize();

    /**
     * Start your web server, if the server is already running, it will call JWebServer.stop() and then start
     */
    void start();

    /**
     * Stop your webserver
     */
    void stop();

    /**
     * Check whether your web server is running or not
     * @return true if your server is running
     */
    boolean isAlive();

    /**
     * Register a new server handler
     * @param handler your web handler
     */
    void registerHandler(Class<? extends WebHandler> handler);

    HandlerManager getHandlerManager();

    /**
     * Create a new server instance
     * @param port the tcp port your server will bind to
     * @return a new server instance
     */
    static JWebServer of(int port) {
        return new TCPServer().port(port);
    }

}
