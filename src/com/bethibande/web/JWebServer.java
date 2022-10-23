package com.bethibande.web;

import com.bethibande.web.annotations.URI;
import com.bethibande.web.cache.Cache;
import com.bethibande.web.cache.CacheConfig;
import com.bethibande.web.cache.CacheLifetimeType;
import com.bethibande.web.cache.CachedRequest;
import com.bethibande.web.context.ContextFactory;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.handlers.InstanceMethodHandler;
import com.bethibande.web.handlers.MethodHandler;
import com.bethibande.web.handlers.StaticMethodHandler;
import com.bethibande.web.handlers.http.HttpHandler;
import com.bethibande.web.handlers.out.ObjectOutputHandler;
import com.bethibande.web.handlers.out.OutputHandler;
import com.bethibande.web.handlers.out.RequestResponseOutputHandler;
import com.bethibande.web.io.ByteArrayWriter;
import com.bethibande.web.io.OutputWriter;
import com.bethibande.web.processors.*;
import com.bethibande.web.processors.impl.*;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.types.CacheType;
import com.bethibande.web.types.ServerCacheConfig;
import com.bethibande.web.types.ServerCacheSupplier;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.types.impl.DefaultCacheSupplierImpl;
import com.bethibande.web.util.ReflectUtils;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// TODO: URI annotation add content-type?
// TODO: implement RequestMethod URI annotation parameter
// TODO: update RequestMethod enum
// TODO: remove HttpHandler timing debug message
// TODO: fireBeforeInvocationHandlers and fireAfterInvocationHandlers methods?
// TODO: abstract MethodInvocationHandler and abstract AnnotationInvocationHandler
// TODO: performance improvements?
// TODO: @Secured method annotation for example
public class JWebServer {

    private InetSocketAddress bindAddress;

    private HttpServer server;

    /**
     * @see #isDebug()
     */
    private boolean debug = false;

    private ServerCacheConfig cacheConfig;
    private Cache<UUID, Session> sessionCache;
    private Cache<String, CachedRequest> globalRequestCache;
    private volatile long lastCacheUpdate = 0;

    private List<ParameterProcessor> processors = new ArrayList<>();
    private HashMap<URI, MethodHandler> methods = new HashMap<>();
    private HashMap<Class<?>, Class<? extends OutputHandler<?>>> outputHandlers = new HashMap<>();
    private HashMap<Class<?>, Class<? extends OutputWriter>> writers = new HashMap<>();
    private List<MethodInvocationHandler> methodInvocationHandlers = new ArrayList<>();

    private ServerCacheSupplier cacheSupplier;
    private ContextFactory contextFactory;

    public JWebServer() {
        initValues();
    }

    private void initValues() {
        bindAddress = new InetSocketAddress("0.0.0.0", 80);

        cacheConfig = new ServerCacheConfig(
                new CacheConfig()
                        .withLifetimeType(CacheLifetimeType.ON_ACCESS)
                        .withMaxItems(100)
                        .withMaxLifetime(TimeUnit.MINUTES.toMillis(10)),
                new CacheConfig()
                        .withLifetimeType(CacheLifetimeType.ON_CREATION)
                        .withMaxLifetime(10000L)
                        .withMaxItems(100),
                new CacheConfig()
                        .withLifetimeType(CacheLifetimeType.ON_CREATION)
                        .withMaxLifetime(10000L)
                        .withMaxItems(10)
        );

        setCacheSupplier(new DefaultCacheSupplierImpl(Cache::new, Cache::new));

        registerMethodInvocationHandler(new CachedRequestHandler());

        registerProcessor(new PathAnnotationProcessor());
        registerProcessor(new SessionParameterProcessor());
        registerProcessor(new ServerContextParameterProcessor());
        registerProcessor(new HeaderValueAnnotationProcessor());
        registerProcessor(new RemoteAddressAnnotationProcessor());

        registerOutputHandler(Object.class, ObjectOutputHandler.class);
        registerOutputHandler(RequestResponse.class, RequestResponseOutputHandler.class);

        registerWriter(byte[].class, ByteArrayWriter.class);

        setContextFactory(ServerContext::new);
    }

    public ServerCacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public ServerCacheSupplier getCacheSupplier() {
        return cacheSupplier;
    }

    public JWebServer withCacheSupplier(ServerCacheSupplier supplier) {
        setCacheSupplier(supplier);
        return this;
    }

    public void setCacheSupplier(ServerCacheSupplier supplier) {
        this.cacheSupplier = supplier;
    }

    public JWebServer withCacheConfig(ServerCacheConfig config) {
        setCacheConfig(config);
        return this;
    }

    public void setCacheConfig(ServerCacheConfig config) {
        this.cacheConfig = config;
    }

    /**
     * @see #isDebug()
     */
    public JWebServer withDebug(boolean debug) {
        setDebug(debug);
        return this;
    }

    /**
     * @see #isDebug()
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * If false, in case of an exception the stacktrace will be discarded and not be printed
     */
    public boolean isDebug() {
        return debug;
    }

    public JWebServer withMethodInvocationHandler(MethodInvocationHandler handler) {
        registerMethodInvocationHandler(handler);
        return this;
    }

    public void registerMethodInvocationHandler(MethodInvocationHandler handler) {
        methodInvocationHandlers.add(handler);
    }

    public List<MethodInvocationHandler> getMethodInvocationHandlers() {
        return methodInvocationHandlers;
    }

    public ContextFactory getContextFactory() {
        return contextFactory;
    }

    public JWebServer withContextFactory(ContextFactory factory) {
        setContextFactory(factory);
        return this;
    }

    public void setContextFactory(ContextFactory factory) {
        this.contextFactory = factory;
    }

    public void updateCache() {
        if(System.currentTimeMillis() - 1000L > lastCacheUpdate) {
            sessionCache.update();
            globalRequestCache.update();
            lastCacheUpdate = System.currentTimeMillis();
        }
    }

    public Session getSession(UUID sessionId) {
        updateCache();
        return sessionCache.get(sessionId);
    }

    public Session getSession(InetAddress owner) {
        updateCache();
        for(UUID sessionId : sessionCache.getAllKeys()) {
            Session session = sessionCache.get(sessionId);
            if(session.getOwner().equals(owner)) return session;
        }
        return null;
    }

    private UUID generateSessionId() {
        UUID id = null;
        while(id == null || sessionCache.hasKey(id)) {
            id = UUID.randomUUID();
        }
        return id;
    }

    public Session generateSession(InetAddress owner) {
        Session session = new Session(
                generateSessionId(),
                this,
                owner
        );

        sessionCache.put(session.getSessionId(), session);

        return session;
    }

    public void handleOutput(RequestResponse response, WebRequest request) {
        Object content = response.getContentData();
        Class<? extends OutputHandler<?>> outputHandler = this.getOutputHandler(content.getClass());
        if(outputHandler == null) outputHandler = this.getOutputHandler(Object.class);
        if(outputHandler == null) throw new InvalidParameterException("There is no output handler that can handle this kind of output: '" + content.getClass() + "'!");

        OutputHandler<Object> out = (OutputHandler<Object>) ReflectUtils.createInstance(outputHandler);
        out.update(content, request);
    }

    public OutputWriter getWriter(Class<?> type) {
        Class<? extends OutputWriter> writerClass = writers.get(type);
        if(writerClass == null) return null;

        return ReflectUtils.createInstance(writerClass);
    }

    public HashMap<Class<?>, Class<? extends OutputWriter>> getWriters() {
        return writers;
    }

    public JWebServer withWriter(Class<?> type, Class<? extends OutputWriter> writer) {
        registerWriter(type, writer);
        return this;
    }

    public void registerWriter(Class<?> type, Class<? extends OutputWriter> writer) {
        writers.remove(type);
        writers.put(type, writer);
    }

    public JWebServer withBindAddress(String address, int port) {
        setBindAddress(new InetSocketAddress(address, port));
        return this;
    }

    /**
     * Binds to 0.0.0.0:port
     */
    public JWebServer withBindAddress(int port) {
        setBindAddress(new InetSocketAddress("0.0.0.0", port));
        return this;
    }

    public JWebServer withHandler(Class<?> handler) {
        registerHandlerClass(handler);
        return this;
    }

    public JWebServer withProcessor(ParameterProcessor processor) {
        registerProcessor(processor);
        return this;
    }

    public <T> JWebServer withOutputHandler(Class<T> type, Class<? extends OutputHandler<T>> handler) {
        registerOutputHandler(type, handler);
        return this;
    }

    public <T> void registerOutputHandler(Class<T> type, Class<? extends OutputHandler<T>> handler) {
        outputHandlers.put(type, handler);
    }

    public <T> Class<? extends OutputHandler<T>> getOutputHandler(Class<T> type) {
        return (Class<? extends OutputHandler<T>>) outputHandlers.get(type);
    }

    public HashMap<Class<?>, Class<? extends OutputHandler<?>>> getOutputHandlers() {
        return outputHandlers;
    }

    public void registerProcessor(ParameterProcessor processor) {
        processors.add(processor);
    }

    public void registerMethod(Method method) {
        URI uri = method.getAnnotation(URI.class);

        if(method.getModifiers() == Modifier.STATIC) {
            this.methods.put(uri, new StaticMethodHandler(method));
        } else {
            this.methods.put(uri, new InstanceMethodHandler(method));
        }
    }

    public void registerHandlerClass(Class<?> handler) {
        for(Method method : handler.getDeclaredMethods()) {
            if(!method.isAnnotationPresent(URI.class)) continue;

            registerMethod(method);
        }
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public Cache<UUID, Session> getSessionCache() {
        return sessionCache;
    }

    public Cache<String, CachedRequest> getGlobalRequestCache() {
        return globalRequestCache;
    }

    public List<ParameterProcessor> getProcessors() {
        return processors;
    }

    public HashMap<URI, MethodHandler> getMethods() {
        return methods;
    }

    public JWebServer withSessionCache(Cache<UUID, Session> cache) {
        this.sessionCache = cache;
        return this;
    }

    public JWebServer withGlobalRequestCache(Cache<String, CachedRequest> cache) {
        this.globalRequestCache = cache;
        return this;
    }

    public JWebServer withBindAddress(InetSocketAddress bindAddress) {
        this.bindAddress = bindAddress;
        return this;
    }

    public void setBindAddress(InetSocketAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    public void setSessionCache(Cache<UUID, Session> sessionCache) {
        this.sessionCache = sessionCache;
    }

    public void setGlobalRequestCache(Cache<String, CachedRequest> globalRequestCache) {
        this.globalRequestCache = globalRequestCache;
    }

    public boolean isAlive() {
        return server != null;
    }

    public void stop() {
        server.stop(0);
        server = null;
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(bindAddress, 100);
            server.createContext("/", new HttpHandler(this));
            start(server);

            this.sessionCache = cacheSupplier.getSessionCache(
                    this,
                    cacheConfig
            );

            this.globalRequestCache = cacheSupplier.getRequestCache(
                    this,
                    cacheConfig,
                    CacheType.GLOBAL_REQUEST_CACHE
            );
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void start(HttpServer server) {
        if(isAlive()) stop();

        this.server = server;
        server.start();
    }

}
