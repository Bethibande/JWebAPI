package com.bethibande.web;

import com.bethibande.web.annotations.AutoLoad;
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
import com.bethibande.web.io.StreamWriter;
import com.bethibande.web.loader.ClassCollector;
import com.bethibande.web.logging.LoggerFactory;
import com.bethibande.web.processors.*;
import com.bethibande.web.processors.impl.*;
import com.bethibande.web.response.InputStreamWrapper;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.types.*;
import com.bethibande.web.types.impl.DefaultCacheSupplierImpl;
import com.bethibande.web.util.ReflectUtils;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bethibande.web.logging.ConsoleColors.*;

/**
 * This class represents a Http or Https Server.<br>
 * This class uses the java HttpServer or alternatively HttpsServer classes to create and run a http server. <br>
 * Default bind address is 0.0.0.0 and port 80
 */
public class JWebServer {

    private InetSocketAddress bindAddress;

    private ScheduledThreadPoolExecutor executor;
    private HttpServer server;

    private Logger logger;
    /**
     * @see #isDebug()
     */
    private boolean debug = false;

    /**
     * Buffer size used by writers
     */
    private int bufferSize = 1024;
    /**
     * Charset used by writers
     */
    private Charset charset = StandardCharsets.UTF_8;
    private Gson gson = new Gson();

    private ServerCacheConfig cacheConfig;
    private Cache<UUID, Session> sessionCache;
    private Cache<String, CachedRequest> globalRequestCache;

    private List<ParameterProcessor> processors = new ArrayList<>();
    //private ArrayMap<URIObject, MethodHandler> methods = new ArrayMap<>(URIObject.class, URIObject[]::new, MethodHandler.class, MethodHandler[]::new);
    private SimpleMap<URIObject, MethodHandler> methods = new SimpleMap<>(URIObject.class, MethodHandler.class);
    private HashMap<Class<?>, OutputHandler<?>> outputHandlers = new HashMap<>();
    private HashMap<Class<?>, Class<? extends OutputWriter>> writers = new HashMap<>();
    private List<MethodInvocationHandler> methodInvocationHandlers = new ArrayList<>();

    private ServerCacheSupplier cacheSupplier;
    private ContextFactory contextFactory;

    public JWebServer() {
        initValues();
    }

    /**
     * Internal method used to initialize default values, caches, processors, handlers, suppliers and more
     */
    private void initValues() {
        executor = new ScheduledThreadPoolExecutor(10);
        logger = LoggerFactory.createLogger(executor);
        logger.setLevel(Level.OFF);

        bindAddress = new InetSocketAddress("0.0.0.0", 80);

        cacheConfig = new ServerCacheConfig(
                new CacheConfig()
                        .withLifetimeType(CacheLifetimeType.ON_ACCESS)
                        .withMaxItems(100)
                        .withMaxLifetime(TimeUnit.MINUTES.toMillis(10))
                        .withUpdateTimeout(TimeUnit.MINUTES.toMillis(1)),
                new CacheConfig()
                        .withLifetimeType(CacheLifetimeType.ON_CREATION)
                        .withMaxLifetime(10000L)
                        .withMaxItems(100)
                        .withUpdateTimeout(TimeUnit.SECONDS.toMillis(2)),
                new CacheConfig()
                        .withLifetimeType(CacheLifetimeType.ON_CREATION)
                        .withMaxLifetime(10000L)
                        .withMaxItems(10)
                        .withUpdateTimeout(TimeUnit.SECONDS.toMillis(1))
        );

        setCacheSupplier(new DefaultCacheSupplierImpl(Cache::new, Cache::new));

        registerMethodInvocationHandler(new URIAnnotationProcessor());
        registerMethodInvocationHandler(new CachedRequestHandler());
        registerMethodInvocationHandler(new BeanHandler());

        registerProcessor(new PathAnnotationProcessor());
        registerProcessor(new SessionParameterProcessor());
        registerProcessor(new ServerContextParameterProcessor());
        registerProcessor(new HeaderValueAnnotationProcessor());
        registerProcessor(new RemoteAddressAnnotationProcessor());
        registerProcessor(new InputStreamParameterProcessor());
        registerProcessor(new QueryFieldAnnotationProcessor());
        registerProcessor(new PostDataAnnotationProcessor());
        registerProcessor(new JsonFieldAnnotationProcessor());
        registerProcessor(new BeanParameterProcessor());

        registerOutputHandler(Object.class, new ObjectOutputHandler());
        registerOutputHandler(RequestResponse.class, new RequestResponseOutputHandler());

        registerWriter(byte[].class, ByteArrayWriter.class);
        registerWriter(InputStreamWrapper.class, StreamWriter.class);

        setContextFactory(ServerContext::new);

        logger.setLevel(Level.INFO);
    }

    /**
     * Destructor, stops the server using {@link #stop()}
     */
    @Override
    protected void finalize() throws Throwable {
        if(!isAlive()) return;

        stop();
    }

    /**
     * Set the executor used by the server.
     * @see #setExecutor(ScheduledThreadPoolExecutor) 
     * @see #getExecutor() 
     */
    public JWebServer withExecutor(ScheduledThreadPoolExecutor executor) {
        setExecutor(executor);
        return this;
    }

    /**
     * Set the executor used by the server.
     * @see #withExecutor(ScheduledThreadPoolExecutor) 
     * @see #getExecutor()
     */
    public void setExecutor(ScheduledThreadPoolExecutor executor) {
        this.executor = executor;
    }

    /**
     * Get the executor used by the server.
     * @see #withExecutor(ScheduledThreadPoolExecutor) 
     * @see #setExecutor(ScheduledThreadPoolExecutor)
     */
    public ScheduledThreadPoolExecutor getExecutor() {
        return this.executor;
    }
    
    /**
     * Set the logger instance used by this server
     * @see #setLogger(Logger)
     */
    public JWebServer withLogger(Logger logger) {
        setLogger(logger);
        return this;
    }

    /**
     * Set the logger instance used by the server
     * @see #getLogger()
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Get the logger instance used by the server
     * @see #setLogger(Logger)
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Set the loggers log level
     * @see #setLogLevel(Level)
     */
    public JWebServer withLogLevel(Level level) {
        setLogLevel(level);
        return this;
    }

    /**
     * Get the loggers log level
     */
    public Level getLogLevel() {
        return this.logger.getLevel();
    }

    /**
     * Set the loggers log level
     */
    public void setLogLevel(Level level) {
        logger.config(String.format("Update LogLevel %s > %s", this.logger.getLevel().getName(), level.getName()));
        this.logger.setLevel(level);
    }

    /**
     * Sets the loggers style
     * @see #setLogStyle(LoggerFactory.LogStyle)
     */
    public JWebServer withLogStyle(LoggerFactory.LogStyle style) {
        setLogStyle(style);
        return this;
    }

    /**
     * Sets the loggers style
     */
    public void setLogStyle(LoggerFactory.LogStyle style) {
        LoggerFactory.setLogStyle(style, this.logger);
    }

    /**
     * Automatically loads and registers all classes annotated with the {@link com.bethibande.web.annotations.AutoLoad} annotation.
     * This only loads classes where the @AutoLoad annotations value is equals the specified module parameter.
     * Works for handlers, parameter processors and method invocation handlers.
     * @param module The value supplied to the @AutoLoad annotation, the annotations value has to be equals to this parameter for the class to be loaded
     * @param root Can be any class that is being loaded from the same location as the classes you want to load.
     *             A location can be anything, a jar file, a directory or more.
     *             A location is being loaded like this -> class.getProtectionDomain().getCodeSource().getLocation()
     */
    public JWebServer autoLoad(Class<?> root, String module) {
        ClassCollector collector = new ClassCollector();
        Collection<Class<?>> classes = collector.collect(root, module);

        for(Class<?> clazz : classes) {
            if(ParameterProcessor.class.isAssignableFrom(clazz)) {
                registerProcessor(ReflectUtils.autoWireNewInstance((Class<? extends ParameterProcessor>) clazz, new ServerContext(this, null, null, null)));
                continue;
            }
            if(MethodInvocationHandler.class.isAssignableFrom(clazz)) {
                registerMethodInvocationHandler(ReflectUtils.autoWireNewInstance((Class<? extends MethodInvocationHandler>) clazz, new ServerContext(this, null, null, null)));
                continue;
            }

            registerHandlerClass(clazz);
        }
        return this;
    }

    /**
     * Automatically loads and registers all classes annotated with the {@link com.bethibande.web.annotations.AutoLoad} annotation.
     * This loads all classes regardless of the value specified in the @AutoLoad annotation.
     * Works for handlers, parameter processors and method invocation handlers.
     * @param root Can be any class that is being loaded from the same location as the classes you want to load.
     *             A location can be anything, a jar file, a directory or more.
     *             A location is being loaded like this -> class.getProtectionDomain().getCodeSource().getLocation()
     */
    public JWebServer autoLoad(Class<?> root) {
        ClassCollector collector = new ClassCollector();
        Collection<Class<?>> classes = collector.collect(root, AutoLoad.class);

        for(Class<?> clazz : classes) {
            if(ParameterProcessor.class.isAssignableFrom(clazz)) {
                registerProcessor(ReflectUtils.autoWireNewInstance((Class<? extends ParameterProcessor>) clazz, new ServerContext(this, null, null, null)));
                continue;
            }
            if(MethodInvocationHandler.class.isAssignableFrom(clazz)) {
                registerMethodInvocationHandler(ReflectUtils.autoWireNewInstance((Class<? extends MethodInvocationHandler>) clazz, new ServerContext(this, null, null, null)));
                continue;
            }

            registerHandlerClass(clazz);
        }
        return this;
    }

    /**
     * Set the globally used gson instance, used read and write json data
     * @see #getGson()
     * @see com.bethibande.web.annotations.PostData
     * @see com.bethibande.web.annotations.JsonField
     */
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    /**
     * Get the globally used gson instance
     * @see #setGson(Gson)
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Sets the servers default charset
     * @return the current JWebServer instance, used for chaining methods.
     * @see #setCharset(Charset)
     */
    public JWebServer withCharset(Charset charset) {
        setCharset(charset);
        return this;
    }

    /**
     * Sets the servers buffer size, used for writing and reading data
     * @param bufferSize buffer size in bytes
     * @return the current JWebServer instance, used for chaining methods.
     * @see #setBufferSize(int) 
     */
    public JWebServer withBufferSize(int bufferSize) {
        setBufferSize(bufferSize);
        return this;
    }

    /**
     * Sets the servers default charset
     * @param charset default value is UTF-8
     * @see #withCharset(Charset)
     */
    public void setCharset(Charset charset) {
        logger.config(String.format("Update Charset %s > %s", this.charset.displayName(), charset.displayName()));
        this.charset = charset;
    }

    /**
     * Sets the servers buffer size, used for reading and writing data
     * @param bufferSize buffer size in bytes, default value is 1024
     * @see #withBufferSize(int)
     */
    public void setBufferSize(int bufferSize) {
        logger.config(String.format("Update BufferSize %d > %d", this.bufferSize, bufferSize));
        this.bufferSize = bufferSize;
    }

    /**
     * Gets the servers buffer size
     * @return buffer size in bytes
     * @see #setBufferSize(int)
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Gets the servers default charset
     * @return default value is UTF-8
     * @see #setCharset(Charset)
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Gets the config used to configure caches
     */
    public ServerCacheConfig getCacheConfig() {
        return cacheConfig;
    }

    /**
     * Get cache supplier
     * @see #setCacheSupplier(ServerCacheSupplier)
     */
    public ServerCacheSupplier getCacheSupplier() {
        return cacheSupplier;
    }

    /**
     * Set the cache supplier, used to create new cache instances
     * @return the current JWebServer instance, used for chaining methods.
     * @see #setCacheSupplier(ServerCacheSupplier)
     */
    public JWebServer withCacheSupplier(ServerCacheSupplier supplier) {
        setCacheSupplier(supplier);
        return this;
    }

    /**
     * Set the cache supplier, used to create new cache instances
     */
    public void setCacheSupplier(ServerCacheSupplier supplier) {
        logger.config("Update CacheSupplier");
        this.cacheSupplier = supplier;
    }

    /**
     * Set the cache config, used to configure all caches used by the server.
     * @return the current JWebServer instance, used for chaining methods.
     * @see #setCacheConfig(ServerCacheConfig)
     */
    public JWebServer withCacheConfig(ServerCacheConfig config) {
        setCacheConfig(config);
        return this;
    }

    /**
     * Set the cache config, used to configure all caches used by this server.
     */
    public void setCacheConfig(ServerCacheConfig config) {
        logger.config("Update CacheConfig");
        this.cacheConfig = config;
    }

    /**
     * @see #isDebug()
     */
    @Deprecated(forRemoval = true)
    public JWebServer withDebug(boolean debug) {
        setDebug(debug);
        return this;
    }

    /**
     * @see #isDebug()
     */
    @Deprecated(forRemoval = true)
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * If false, in case of an exception the stacktrace will be discarded and not be printed.
     */
    @Deprecated(forRemoval = true)
    public boolean isDebug() {
        return debug;
    }

    /**
     * Register an invocation handler, fired before and after invoking a method.
     * @return the current JWebServer instance, used for chaining methods.
     * @see #registerMethodInvocationHandler(MethodInvocationHandler)
     */
    public JWebServer withMethodInvocationHandler(MethodInvocationHandler handler) {
        registerMethodInvocationHandler(handler);
        return this;
    }

    /**
     * Register an invocation handler, fired before and after invoking a method.
     */
    public void registerMethodInvocationHandler(MethodInvocationHandler handler) {
        logger.config(String.format("Register MethodInvocationHandler > %s", handler.getClass().getName()));
        methodInvocationHandlers.add(handler);
    }

    /**
     * Get all registered method invocation handlers
     * @see #registerMethodInvocationHandler(MethodInvocationHandler)
     */
    public List<MethodInvocationHandler> getMethodInvocationHandlers() {
        return methodInvocationHandlers;
    }

    /**
     * @return Context factory used to create context instances
     */
    public ContextFactory getContextFactory() {
        return contextFactory;
    }

    /**
     * Set the context factory
     * @see #getContextFactory()
     * @see #setContextFactory(ContextFactory)
     */
    public JWebServer withContextFactory(ContextFactory factory) {
        setContextFactory(factory);
        return this;
    }

    /**
     * Set the context factory used to create new context instances
     * @see #getContextFactory()
     */
    public void setContextFactory(ContextFactory factory) {
        logger.config(String.format("Update ContextFactory > %s", factory.getClass().getName()));
        this.contextFactory = factory;
    }

    /**
     * Call {@link Cache#update()} method of session and global request cache.<br>
     * Update method will only be called every 1000 ms.
     */
    public void updateCache() {
        boolean sessionUpdate = sessionCache.update();
        boolean globalUpdate = globalRequestCache.update();
        if(sessionUpdate || globalUpdate) logger.finest("Cache Update");
    }

    /**
     * Calls {@link #updateCache()} and returns session belonging to the ip address.<br>
     * Returns null if there is no session.
     */
    public Session getSession(InetAddress owner) {
        this.updateCache();
        for(UUID sessionId : sessionCache.getAllKeys()) {
            Session session = sessionCache.get(sessionId);
            if(session.getOwner().equals(owner)) {
                return session;
            }
        }
        return null;
    }

    /**
     * Internal method used to generate session ids.
     */
    private UUID generateSessionId() {
        UUID id = null;
        while(id == null || sessionCache.hasKey(id)) {
            id = UUID.randomUUID();
        }
        return id;
    }

    /**
     * Create a new session for the specified ip address.
     * The created session will be stored in the session cache
     * @return the newly created session
     */
    public Session generateSession(InetAddress owner) {
        Session session = new Session(
                generateSessionId(),
                this,
                owner
        );

        sessionCache.put(session.getSessionId(), session);

        logger.fine(String.format("Create Session > %s %s", annotate(session.getSessionId().toString(), ORANGE), annotate(owner.getHostAddress(), BLUE)));

        return session;
    }

    /**
     * Get writer for the specified type, returns null if there isn't one.
     */
    public OutputWriter getWriter(Class<?> type) {
        Class<? extends OutputWriter> writerClass = writers.get(type);
        if(writerClass == null) return null;

        return ReflectUtils.createInstance(writerClass);
    }

    /**
     * Get a map of all writers and the types of objects they can write
     */
    public HashMap<Class<?>, Class<? extends OutputWriter>> getWriters() {
        return writers;
    }

    /**
     * Register a new writer for a certain type.
     * @return the current JWebServer instance, used for chaining methods.
     */
    public JWebServer withWriter(Class<?> type, Class<? extends OutputWriter> writer) {
        registerWriter(type, writer);
        return this;
    }

    public void registerWriter(Class<?> type, Class<? extends OutputWriter> writer) {
        logger.config(String.format("Register Writer > %s for type %s", writer.getName(), type.getName()));
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

    public <T> JWebServer withOutputHandler(Class<T> type, OutputHandler<T> handler) {
        registerOutputHandler(type, handler);
        return this;
    }

    public <T> void registerOutputHandler(Class<T> type, OutputHandler<T> handler) {
        logger.config(String.format("Register OutputHandler > %s for type %s", handler.getClass().getName(), type.getName()));
        outputHandlers.put(type, handler);
    }

    public <T> OutputHandler<T> getOutputHandler(Class<T> type) {
        return (OutputHandler<T>) outputHandlers.get(type);
    }

    public HashMap<Class<?>, OutputHandler<?>> getOutputHandlers() {
        return outputHandlers;
    }

    public void registerProcessor(ParameterProcessor processor) {
        logger.config(String.format("Register ParameterProcessor > %s", processor.getClass().getName()));
        processors.add(processor);
    }

    public JWebServer withMethod(Class<?> clazz, String methodName, Class<?>... methodSignature) {
        registerMethod(clazz, methodName, methodSignature);
        return this;
    }

    public JWebServer withMethod(Method method) {
        registerMethod(method);
        return this;
    }

    public void registerMethod(Class<?> clazz, String methodName, Class<?>... methodSignature) {
        try {
            registerMethod(clazz.getDeclaredMethod(methodName, methodSignature));
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void registerMethod(Method method) {
        URI uri = method.getAnnotation(URI.class);
        logger.finest(String.format("Register Method > %s:%s %s %s", method.getDeclaringClass().getName(), method.getName(), uri.value(), Arrays.toString(uri.methods())));

        if(method.getModifiers() == Modifier.STATIC) {
            this.methods.put(URIObject.of(uri), new StaticMethodHandler(method));
        } else {
            this.methods.put(URIObject.of(uri), new InstanceMethodHandler(method));
        }
    }

    public void registerHandlerClass(Class<?> handler) {
        logger.config(String.format("Register Class > %s", handler.getName()));
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

    public SimpleMap<URIObject, MethodHandler> getMethods() {
        return methods;
    }

    public JWebServer withBindAddress(InetSocketAddress bindAddress) {
        setBindAddress(bindAddress);
        return this;
    }

    public void setBindAddress(InetSocketAddress bindAddress) {
        logger.config(String.format("Set BindAddress > %s", annotate(bindAddress.toString().substring(1), BLUE)));
        this.bindAddress = bindAddress;
    }

    public boolean isAlive() {
        return server != null;
    }

    public void stop() {
        server.stop(0);
        server = null;

        logger.info(String.format("%s stopped", annotate("JWebAPI Server", BLUE + BOLD)));
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(bindAddress, 100);
            start(server);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void start(HttpServer server) {
        if(isAlive()) stop();

        this.server = server;
        server.setExecutor(executor);
        server.start();

        this.sessionCache = cacheSupplier.getSessionCache(
                this,
                cacheConfig
        );

        this.globalRequestCache = cacheSupplier.getRequestCache(
                this,
                cacheConfig,
                CacheType.GLOBAL_REQUEST_CACHE
        );

        server.createContext("/", new HttpHandler(this));

        logger.info(String.format("%s started on %s", annotate("JWebAPI Server", BLUE + BOLD), annotate(bindAddress.toString().substring(1), MAGENTA)));
    }

}

