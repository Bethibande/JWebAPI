package com.bethibande.web;

import com.bethibande.web.cache.Cache;
import com.bethibande.web.cache.CacheConfig;
import com.bethibande.web.cache.CacheLifetimeType;
import com.bethibande.web.cache.CachedRequest;
import com.bethibande.web.handlers.client.ClientHandler;
import com.bethibande.web.logging.LoggerFactory;
import com.bethibande.web.processors.MethodInvocationHandler;
import com.bethibande.web.processors.client.ClientHeaderProcessor;
import com.bethibande.web.processors.client.ClientParameterProcessor;
import com.bethibande.web.processors.client.ClientQueryProcessor;
import com.bethibande.web.processors.client.PostDataProcessor;
import com.bethibande.web.processors.impl.CachedRequestHandler;
import com.bethibande.web.processors.impl.URIAnnotationHandler;
import com.bethibande.web.readers.JsonReader;
import com.bethibande.web.readers.StreamReader;
import com.bethibande.web.response.InputStreamWrapper;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.Request;
import com.bethibande.web.types.ResponseReader;
import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class JWebClient implements JWebAPI {

    /**
     * Executor needed for logging
     */
    private ThreadPoolExecutor executor;
    private Logger logger;

    private URL baseUrl;
    private int bufferSize = 1024;
    private Charset charset = StandardCharsets.UTF_8;
    private Gson gson = new Gson();

    private final HashMap<Class<?>, WeakReference<Object>> repositories = new HashMap<>();

    private final List<MethodInvocationHandler> invocationHandlers = new ArrayList<>();

    private Cache<String, CachedRequest> requestCache;
    private CacheConfig cacheConfig;

    private final HashMap<Class<?>, ResponseReader> readers = new HashMap<>();

    private final List<ClientParameterProcessor> processors = new ArrayList<>();

    public JWebClient() {
        this.init();
    }

    private void init() {
        executor = new ThreadPoolExecutor(1, 1, 60000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1000));
        logger = LoggerFactory.createLogger(this);
        logger.setLevel(Level.ALL);

        cacheConfig = new CacheConfig(
                CacheLifetimeType.ON_CREATION,
                TimeUnit.SECONDS.toMillis(10),
                100,
                TimeUnit.SECONDS.toMillis(2)
        );

        requestCache = new Cache<>();
        requestCache.apply(cacheConfig);

        registerMethodInvocationHandler(new URIAnnotationHandler());
        registerMethodInvocationHandler(new CachedRequestHandler());

        registerProcessor(new ClientQueryProcessor());
        registerProcessor(new ClientHeaderProcessor());
        registerProcessor(new PostDataProcessor());

        registerReader(Object.class, new JsonReader(this));
        registerReader(InputStream.class, new StreamReader(this));
        registerReader(InputStreamWrapper.class, new StreamReader(this));
    }

    /**
     * Register a new processor used to infer repository parameters
     * @return current client instance
     */
    @Contract("_->this")
    public JWebClient withProcessor(final ClientParameterProcessor processor) {
        registerProcessor(processor);
        return this;
    }

    /**
     * Register a new processor used to infer repository parameters
     */
    public void registerProcessor(final ClientParameterProcessor processor) {
        processors.add(processor);
        logger.config(String.format("Registered processor > %s", processor.getClass().getName()));
    }

    /**
     * List of processors used to infer repository parameters
     */
    public List<ClientParameterProcessor> getProcessors() {
        return processors;
    }

    /**
     * Used to destroy a client instance.
     * Shuts down the thread-pool-executor
     */
    public void destroy() {
        executor.shutdown();
    }

    /**
     * Register a new response reader, used to read server responses and turn them into the required java types
     * @return current client instance
     */
    public JWebClient withReader(final Class<?> type, final ResponseReader reader) {
        registerReader(type, reader);
        return this;
    }

    /**
     * Register a new response reader, used to read server responses and turn them into the required java types
     */
    public void registerReader(final Class<?> type, final ResponseReader reader) {
        readers.put(type, reader);
        logger.config(String.format("Registered response reader > %s for type %s",reader.getClass().getName(), type.getTypeName()));
    }

    /**
     * Get all response readers, used to read server responses and turn them into the required java types
     */
    public HashMap<Class<?>, ResponseReader> getReaders() {
        return readers;
    }

    /**
     * Get request cache, used by CachedRequest annotation to cache request responses
     */
    @Override
    public Cache<String, CachedRequest> getRequestCache() {
        return requestCache;
    }

    /**
     * Register a new method invocation handler used to handle repository method invocations
     * @return current client instance
     */
    @Override
    @Contract("_->this")
    public JWebAPI withMethodInvocationHandler(final MethodInvocationHandler handler) {
        registerMethodInvocationHandler(handler);
        return this;
    }

    /**
     * Register a new method invocation handler used to handle repository method invocations
     */
    @Override
    public void registerMethodInvocationHandler(final MethodInvocationHandler handler) {
        this.invocationHandlers.add(handler);
        logger.config(String.format("Registered invocation handler > %s", handler.getClass().getName()));
    }

    /**
     * Get all method invocation handlers used to handle repository method invocations
     */
    @Override
    public List<MethodInvocationHandler> getMethodInvocationHandlers() {
        return invocationHandlers;
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
    @Override
    public void setGson(final Gson gson) {
        this.gson = gson;
        this.logger.config("Update gson instance");
    }

    /**
     * @return gson instance used by this client
     * @see #setGson(Gson)
     * @see #withGson(Gson)
     */
    @Override
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
    @Override
    public void setCharset(final Charset charset) {
        this.logger.config(String.format("Update charset %s > %s", this.charset.name(), charset.name()));
        this.charset = charset;
    }

    /**
     * Get the charset used by the client, default is UTF-8
     */
    @Override
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
     * @return current client instance
     */
    @Contract("_->this")
    public JWebClient withBaseUrl(final String baseUrl) {
        setBaseUrl(baseUrl);
        return this;
    }

    /**
     * Set the base url, used by repository classes
     * @param baseUrl must be http or https url
     */
    public void setBaseUrl(final String baseUrl) {
        if(!baseUrl.toLowerCase().matches("http(s?)://.+")) throw new RuntimeException("Must be http or https url");
        try {
            this.baseUrl = new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        logger.config(String.format("Set base url > %s", baseUrl));
    }

    /**
     * Get the base url used by repository classes
     */
    public URL getBaseUrl() {
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
     * Note, parameter processors registered after calling this method, will not be applied to the resulting repository.
     */
    @SuppressWarnings("unchecked")
    public <T> T withRepository(final Class<T> type) {
        if(!Modifier.isInterface(type.getModifiers())) throw new RuntimeException("Only interfaces allowed here.");

        final InvocationHandler handler = new ClientHandler(this, type);
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
    @Override
    public void setLogger(final @NotNull Logger logger) {
        this.logger = logger;
        logger.config(String.format("Set Logger > %s", logger.getClass().getName()));
    }

    /**
     * Get Logger instance used by the client
     */
    @Override
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
     * will have to be terminated manually using {@link RequestResponse#disconnect()}.
     * If the request uri is not absolute (for example "/test"), this method will concatenate it with the clients base url,
     * <b>request url = baseUrl + uri</b> <br>
     * Important: call {@link RequestResponse#disconnect()} to close the connection, if response content length != 0
     */
    public RequestResponse sendRequest(final Request request) {
        try {
            final URL url = request.uri().isAbsolute() ? request.uri().toURL(): new URL(baseUrl, request.uri().toString());

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
                connection.setRequestProperty(
                        "Content-Length",
                        String.valueOf(request.writer().getLength())
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
