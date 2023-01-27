package com.bethibande.web;

import com.bethibande.web.logging.LoggerFactory;
import com.bethibande.web.response.InputStreamWrapper;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.HasExecutor;
import com.bethibande.web.types.Request;
import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private int bufferSize = 1024;
    private Charset charset = StandardCharsets.UTF_8;
    private Gson gson = new Gson();

    private final HashMap<Class<?>, WeakReference<Object>> repositories = new HashMap<>();

    public JWebClient() {
        this.init();
    }

    private void init() {
        executor = new ThreadPoolExecutor(1, 5, 60000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1000));
        logger = LoggerFactory.createLogger(this);
    }


    /**
     * Set the gson instance used by the client
     * @return current client instance
     * @see #getGson()
     * @see #setGson(Gson)
     */
    @Contract("_->this")
    public JWebClient withGson(final Gson gson) {
        setGson(gson);
        return this;
    }

    /**
     * Set the gson instance used by the client
     * @see #getGson()
     * @see #withGson(Gson)
     */
    public void setGson(final Gson gson) {
        this.gson = gson;
        this.logger.config("Update gson instance");
    }

    /**
     * @return gson instance used by this client
     * @see #setGson(Gson)
     * @see #withGson(Gson)
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Set the charset used by the client, default is UTF-8
     * @return current client instance
     */
    @Contract("_->this")
    public JWebClient withCharset(final Charset charset) {
        setCharset(charset);
        return this;
    }

    /**
     * Set the charset used by the client, default is UTF-8
     */
    public void setCharset(final Charset charset) {
        this.logger.config(String.format("Update charset %s > %s", this.charset.name(), charset.name()));
        this.charset = charset;
    }

    /**
     * Get the charset used by the client, default is UTF-8
     */
    public Charset getCharset() {
        return this.charset;
    }

    /**
     * Set the buffer size used to read/write data
     * @param bufferSize  buffer size in bytes
     * @return current client instance
     */
    @Contract("_->this")
    public JWebClient withBufferSize(final int bufferSize) {
        setBufferSize(bufferSize);
        return this;
    }

    /**
     * Set the buffer size used to read/write data
     * @param bufferSize  buffer size in bytes
     */
    public void setBufferSize(final int bufferSize) {
        logger.config(String.format("Update Buffer Size %d > %d", this.bufferSize, bufferSize));
        this.bufferSize = bufferSize;
    }

    /**
     * Get the buffer size used to read/write data
     * @return buffer size in bytes
     */
    public int getBufferSize() {
        return this.bufferSize;
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
     * Send a http/https request and get the response body as a json object.
     * This method will try to transform the response body into an object of the given type using gson.
     * This method assumes that the response body is json text, a MalformedJsonException will be thrown if not. <br>
     * constraints: return value v -> <b>v = null</b> or <b>v.getContentData() instanceof type</b>
     * @param request the request to send
     * @param type json will be deserialized into this type of object
     * @return a RequestResponse Object with an Object of the given type as its content data,
     *         or null in case of an invalid request/response
     */
    public RequestResponse getJson(final Request request, final Type type) {
        final RequestResponse response =  getString(request);
        if(response == null) return null;

        final String json = (String) response.getContentData();
        if(json == null) return null;

        return response.withContentData(gson.fromJson(json, type));
    }

    /**
     * Send a http/https request and get the response body as a text using the configured charset.
     * Uses {@link #sendRequest(Request)}
     * @param request the request/url that will return text as its response body
     * @return  a RequestResponse Object with a String as its content data or null in case of an invalid request/response
     */
    public RequestResponse getString(final Request request) {
        final RequestResponse response = sendRequest(request);

        long contentLength = response.getContentLength();
        if(contentLength <= 0) {
            response.disconnect();
            return null;
        }

        if(!(response.getContentData() instanceof InputStreamWrapper inputStreamWrapper)) {
            throw new RuntimeException("Internal error, invalid response");
        }

        final InputStream in = inputStreamWrapper.getStream();

        final StringBuilder sb = new StringBuilder();

        try {
            while (contentLength > 0) {
                final int length = (int) Math.min(contentLength, bufferSize);
                final byte[] data = in.readNBytes(length);

                sb.append(new String(data, charset));

                contentLength -= length;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        response.disconnect();

        response.setContentData(sb.toString());

        return response;
    }

    /**
     * Send a http or https request.
     * The response data will contain a InputStreamWrapper instance to read the received data from.
     * If the response content length is 0, this method will close the connection automatically, otherwise the connection
     * will have to be terminated manually using {@link RequestResponse#disconnect()}
     * Important: call {@link RequestResponse#disconnect()} to close the connection, if response content length != 0
     */
    public RequestResponse sendRequest(final Request request) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) request.url().openConnection();
            connection.setRequestMethod(request.method().name().toUpperCase());
            connection.setDoOutput(request.writer() != null && request.writer().getLength() > 0);
            connection.setUseCaches(false);
            connection.setDoInput(true);

            request.headers().forEach((key, list) -> {
                for(String value : list) {
                    connection.addRequestProperty(key, value);
                }
            });

            if(request.writer() != null) {
                connection.getRequestProperties().put(
                        "Content-Length",
                        List.of(String.valueOf(request.writer().getLength()))
                );
            }
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
