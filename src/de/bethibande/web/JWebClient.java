package de.bethibande.web;

import de.bethibande.web.handlers.ClientHandleManager;
import de.bethibande.web.tcp.ClientHandler;

import java.lang.reflect.Proxy;

public class JWebClient<T> {

    private String baseUrl;
    private ClientHandleManager clientHandleManager;
    private ClientHandler handler;

    private final T instance;

    private int bufferSize = 1024;

    private JWebClient(String baseUrl, ClientHandler handler, T instance) {
        this.baseUrl = baseUrl;
        this.handler = handler;
        this.instance = instance;
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

    public JWebClient<T> handler(ClientHandler handler) {
        this.handler = handler;
        return this;
    }

    public ClientHandler getHandler() {
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
        ClientHandler handler = new ClientHandler();
        T instance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, handler);

        JWebClient<T> client = new JWebClient<>(baseUrl, handler, instance);
        client.handle(clazz);
        client.baseUrl(baseUrl);

        handler.setClient(client);

        return client;
    }

}
