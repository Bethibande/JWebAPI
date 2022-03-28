package de.bethibande.web;

import de.bethibande.web.handlers.HandlerManager;
import de.bethibande.web.handlers.WebHandler;
import de.bethibande.web.tcp.TCPServer;

// TODO: protocol selection (TCP/UDP, UDP has no ssl support)
// TODO: set server charset
// TODO: ssl encryption
// TODO: field annotations: URIField(field) = get fields from uri, define uri fields within the uri annotation uri = (/test/{field-name})
// TODO: field annotations: ContentLength = HeaderField(value = "Content-Length", def = "0")
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

    static JWebServer tcp(int port) {
        return new TCPServer().port(port);
    }

}
