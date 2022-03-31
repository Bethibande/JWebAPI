package de.bethibande.web;

import de.bethibande.web.handlers.ClientHandleManager;
import de.bethibande.web.tcp.ClientHandler;

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

    public JWebClient<T> setInstance(T instance) {
        this.instance = instance;
        return this;
    }

    public JWebClient<T> charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public T getInstance() {
        return instance;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public JWebClient<T> setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public JWebClient<T> handler(ClientHandler<T> handler) {
        this.handler = handler;
        return this;
    }

    public ClientHandler<T> getHandler() {
        return this.handler;
    }

    public void handle(Class<T> clazz) {
        clientHandleManager = ClientHandleManager.of(clazz);
    }

    public ClientHandleManager getClientHandleManager() {
        return this.clientHandleManager;
    }

    public JWebClient<T> baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }


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
