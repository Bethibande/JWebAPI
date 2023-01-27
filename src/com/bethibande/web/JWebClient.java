package com.bethibande.web;

import com.bethibande.web.logging.LoggerFactory;
import com.bethibande.web.response.InputStreamWrapper;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.HasExecutor;
import com.bethibande.web.types.Request;
import com.sun.net.httpserver.Headers;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class JWebClient implements HasExecutor {

    private ThreadPoolExecutor executor;
    private Logger logger;

    private String baseUrl;

    private final HashMap<Class<?>, WeakReference<Object>> repositories = new HashMap<>();

    public JWebClient() {
        this.init();
    }

    private void init() {
        executor = new ThreadPoolExecutor(1, 5, 60000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1000));
        logger = LoggerFactory.createLogger(this);
    }

    /**
     * Set the base url, used by repository classes
     * @param baseUrl must be http or https url
     */
    public void setBaseUrl(final String baseUrl) {
        if(!baseUrl.toLowerCase().matches("http(s?)://.+")) throw new RuntimeException("Must be http or https url");
        this.baseUrl = baseUrl;
        logger.config(String.format("Set base url > %s", baseUrl));
    }

    /**
     * Get the base url used by repository classes
     */
    public String getBaseUrl() {
        if(baseUrl == null) throw new RuntimeException("No base url has been set.");
        return baseUrl;
    }

    /**
     * Returns an instance of the given repository class. Will null if you have never created an instance of your repository
     * or null if the repository was collected by the garbage collector
     * @return repository instance, may be null
     */
    @SuppressWarnings("unchecked")
    public <T> T getRepository(final Class<T> type) {
        final WeakReference<T> reference = (WeakReference<T>) repositories.get(type);
        if(reference == null) return null;

        final T instance = reference.get();
        if(instance == null) repositories.remove(type);

        return instance;
    }

    /**
     * Creates an instance of your repository class. A repository class is an interface.
     * Additionally, this stores a reference to the instance that can be retrieved using {@link #getRepository(Class)}.
     * This reference is a WeakReference to prevent memory leaks.
     */
    @SuppressWarnings("unchecked")
    public <T> T withRepository(final Class<T> type) {
        final InvocationHandler handler = null; // TODO: create invocation handler
        final T instance = (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
        repositories.put(type, new WeakReference<>(instance));

        return instance;
    }

    /**
     * Set ThreadPoolExecutor used by this client instance
     * @return the current client instance
     */
    @Contract(value = "_->this")
    public JWebClient withExecutor(final @NotNull ThreadPoolExecutor executor) {
        setExecutor(executor);
        return this;
    }

    /**
     * Set ThreadPoolExecutor used by this client instance
     */
    @Override
    public void setExecutor(final @NotNull ThreadPoolExecutor executor) {
        this.executor = executor;
        logger.config(String.format("Set Executor > %s", executor.getClass().getName()));
    }

    /**
     * Get ThreadPoolExecutor used by this client instance
     */
    @Override
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * Set the Logger instance used by the client
     * @return current client instance
     */
    @Contract("_->this")
    public JWebClient withLogger(final @NotNull Logger logger) {
        setLogger(logger);
        return this;
    }

    /**
     * Set the Logger instance used by the client
     */
    public void setLogger(final @NotNull Logger logger) {
        this.logger = logger;
        logger.config(String.format("Set Logger > %s", logger.getClass().getName()));
    }

    /**
     * Get Logger instance used by the client
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Send a http or https request.
     * The response data will contain a InputStreamWrapper instance to read the received data from.
     * If the response content length is 0, this method will close the connection automatically, otherwise the connection
     * will have to be terminated manually using {@link RequestResponse#disconnect()}
     * Important: call {@link RequestResponse#disconnect()} to close the connection, if response content length != 0
     */
    public RequestResponse sendRequest(final Request request, final int bufferSize) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) request.url().openConnection();
            connection.setRequestMethod(request.method().name().toUpperCase());
            connection.setDoInput(request.writer() != null && request.writer().getLength() > 0);
            connection.getRequestProperties().putAll(request.headers());
            if(request.writer() != null) {
                connection.getRequestProperties().put(
                        "Content-Length",
                        List.of(String.valueOf(request.writer().getLength()))
                );
            }
            connection.setDoOutput(true);
            connection.connect();

            if(request.writer() != null && request.writer().getLength() > 0) {
                final OutputStream out = connection.getOutputStream();

                while(request.writer().hasNext()) {
                    request.writer().write(out, bufferSize);
                    out.flush();
                }
            }

            final int responseCode = connection.getResponseCode();
            final long contentLength = connection.getContentLengthLong();

            final Headers headers = new Headers();
            headers.putAll(connection.getHeaderFields());

            if(contentLength == 0) {
                connection.disconnect();
            }

            return new RequestResponse()
                    .withConnection(connection)
                    .withStatusCode(responseCode)
                    .withContentLength(contentLength)
                    .withContentData(new InputStreamWrapper(connection.getInputStream(), contentLength))
                    .withHeader(headers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
