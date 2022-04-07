package com.bethibande.web;

import com.bethibande.web.handlers.ClientHandleManager;
import com.bethibande.web.tcp.ClientHandler;

import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JWebClient<T> {

    private String baseUrl;
    private ClientHandleManager clientHandleManager;
    private ClientHandler<T> handler;
    private Charset charset = StandardCharsets.UTF_8;

    private T instance;

    private int bufferSize = 1024;

    /**
     * Internal method, do not call
     */
    public JWebClient<T> setInstance(T instance) {
        this.instance = instance;
        return this;
    }

    /**
     * Change the charset used for http requests
     * @param charset the new charset
     * @return your client instance
     */
    public JWebClient<T> charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Get the charset used to create http requests
     * @return your charset, default StandardCharsets.UTF_8
     */
    public Charset getCharset() {
        return this.charset;
    }

    /**
     * Used to retrieve the instance of your interface, passed into the JWebClient.of(Class, url); method
     * @return your class instance
     */
    public T getInstance() {
        return instance;
    }

    /**
     * Set the buffer size used for http requests
     * @return buffer size
     */
    public int getBufferSize() {
        return this.bufferSize;
    }

    /**
     * Set the buffer size used for http requests
     * @param bufferSize the new buffer size
     * @return your client instance
     */
    public JWebClient<T> setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * Internal method, do not call
     */
    public JWebClient<T> handler(ClientHandler<T> handler) {
        this.handler = handler;
        return this;
    }

    /**
     * Only here for completion, no real use, will probably be removed soon
     * @return handler instance
     */
    @Deprecated(forRemoval = true)
    public ClientHandler<T> getHandler() {
        return this.handler;
    }

    /**
     * Internal method, do not call
     */
    public void handle(Class<T> clazz) {
        clientHandleManager = ClientHandleManager.of(clazz);
    }

    /**
     * Internal method
     * @return Client handle, containing some cached info on the methods of this client
     */
    public ClientHandleManager getClientHandleManager() {
        return this.clientHandleManager;
    }

    /**
     * Change the base url used to make http requests, must be valid http/https url or it won't work
     * @param baseUrl the base url like 'http://your-api-server.com/api' or 'http://127.0.0.1:43568/'
     * @return your client instance
     */
    public JWebClient<T> baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Get the current base url
     * @return the url
     */
    public String getBaseUrl() {
        return this.baseUrl;
    }


    /**
     * Create a client instance
     * @param clazz an interface declaring all of your methods, retrieve interface instance using <br> JWebClient.getInstance()
     * @param baseUrl the base url used to form http requests like 'http://your-api-server.com/api' or 'http://127.0.0.1:43568/'
     * @return a new client instance, containing an instance of your 'clazz' interface
     */
    public static <T> JWebClient<T> of(Class<T> clazz, String baseUrl) {
        ClientHandler<T> handler = new ClientHandler<>();
        T instance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, handler);

        JWebClient<T> client = new JWebClient<>();
        client.setInstance(instance);
        client.handler(handler)
                .baseUrl(baseUrl)
                .handle(clazz);

        handler.setClient(client);

        return client;
    }

}
