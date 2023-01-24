package com.bethibande.web;

import com.bethibande.web.annotations.AutoLoad;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.beans.GlobalBean;
import com.bethibande.web.beans.GlobalBeanManager;
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
import com.bethibande.web.processors.MethodInvocationHandler;
import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.processors.impl.BeanHandler;
import com.bethibande.web.processors.impl.BeanParameterProcessor;
import com.bethibande.web.processors.impl.CachedRequestHandler;
import com.bethibande.web.processors.impl.GlobalBeanParameterProcessor;
import com.bethibande.web.processors.impl.HeaderValueAnnotationProcessor;
import com.bethibande.web.processors.impl.InputStreamParameterProcessor;
import com.bethibande.web.processors.impl.JsonFieldAnnotationProcessor;
import com.bethibande.web.processors.impl.PathAnnotationProcessor;
import com.bethibande.web.processors.impl.PostDataAnnotationProcessor;
import com.bethibande.web.processors.impl.QueryFieldAnnotationProcessor;
import com.bethibande.web.processors.impl.RemoteAddressAnnotationProcessor;
import com.bethibande.web.processors.impl.ServerContextParameterProcessor;
import com.bethibande.web.processors.impl.SessionParameterProcessor;
import com.bethibande.web.processors.impl.URIAnnotationProcessor;
import com.bethibande.web.response.InputStreamWrapper;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.types.CacheType;
import com.bethibande.web.types.ServerInterface;
import com.bethibande.web.types.ProcessorMappings;
import com.bethibande.web.types.ServerCacheConfig;
import com.bethibande.web.types.ServerCacheSupplier;
import com.bethibande.web.types.SimpleMap;
import com.bethibande.web.types.URIObject;
import com.bethibande.web.types.impl.DefaultCacheSupplierImpl;
import com.bethibande.web.util.ReflectUtils;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bethibande.web.logging.ConsoleColors.*;

/**
 * This class represents a Http or Https Server.<br>
 * This class uses the java HttpServer or alternatively HttpsServer classes to create and run a http server. <br>
 */
public class JWebServer {

    private ScheduledThreadPoolExecutor executor;
    private final List<ServerInterface> interfaces = new ArrayList<>();

    private Logger logger;

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

    private GlobalBeanManager globalBeanManager;

    private final List<ParameterProcessor> processors = new ArrayList<>();
    private final SimpleMap<URIObject, MethodHandler> methods = new SimpleMap<>(URIObject.class, MethodHandler.class);
    private final HashMap<Class<?>, OutputHandler<?>> outputHandlers = new HashMap<>();
    private final HashMap<Class<?>, Class<? extends OutputWriter>> writers = new HashMap<>();
    private final List<MethodInvocationHandler> methodInvocationHandlers = new ArrayList<>();

    private BiConsumer<Throwable, ServerContext> errorHandler = (th, ctx) -> th.printStackTrace();

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
        logger = LoggerFactory.createLogger(this);
        logger.setLevel(Level.OFF);

        cacheConfig = new ServerCacheConfig(
                new CacheConfig() // session cache
                        .withLifetimeType(CacheLifetimeType.ON_ACCESS)
                        .withMaxItems(100)
                        .withMaxLifetime(TimeUnit.MINUTES.toMillis(10))
                        .withUpdateTimeout(TimeUnit.MINUTES.toMillis(1)),
                new CacheConfig() // global request cache
                        .withLifetimeType(CacheLifetimeType.ON_CREATION)
                        .withMaxLifetime(10000L)
                        .withMaxItems(100)
                        .withUpdateTimeout(TimeUnit.SECONDS.toMillis(2)),
                new CacheConfig() // local/session request cache
                        .withLifetimeType(CacheLifetimeType.ON_CREATION)
                        .withMaxLifetime(10000L)
                        .withMaxItems(10)
                        .withUpdateTimeout(TimeUnit.SECONDS.toMillis(1))
        );

        globalBeanManager = new GlobalBeanManager();

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
        registerProcessor(new GlobalBeanParameterProcessor());

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
    @SuppressWarnings("deprecation")
    protected void finalize() {
        if(!isAlive()) return;

        stop();
    }

    /**
     * @see #setErrorHandler(BiConsumer)
     * @see #getErrorHandler() 
     */
    public JWebServer withErrorHandler(final BiConsumer<Throwable, ServerContext> errorHandler) {
        setErrorHandler(errorHandler);
        return this;
    }
    
    /**
     * Set the error handler used to handle exceptions
     * @see #getErrorHandler() 
     * @see #withErrorHandler(BiConsumer) 
     */
    public void setErrorHandler(final BiConsumer<Throwable, ServerContext> errorHandler) {
        logger.config(String.format("Update Error Handler > %s", errorHandler.getClass()));
        this.errorHandler = errorHandler;
    }

    /**
     * Get the error handler used to handle exceptions
     * @see #setErrorHandler(BiConsumer)
     * @see #withErrorHandler(BiConsumer) 
     */
    public BiConsumer<Throwable, ServerContext> getErrorHandler() {
        return errorHandler;
    }

    /**
     * Store a new global bean instance in the global bean manager
     * @see #getGlobalBean(Class)
     * @see #deleteGlobalBean(Class)
     */
    @SuppressWarnings("unused")
    public void storeGlobalBean(GlobalBean bean) {
        globalBeanManager.storeBean(bean);
    }

    /**
     * Get the instance of a global bean from the global bean manager
     * @see #storeGlobalBean(GlobalBean)
     * @see #deleteGlobalBean(Class)
     */
    @SuppressWarnings("unused")
    public <T extends GlobalBean> T getGlobalBean(Class<T> type) {
        return globalBeanManager.getBean(type);
    }

    /**
     * Delete a bean from the global bean manager
     * @see #storeGlobalBean(GlobalBean)
     * @see #getGlobalBean(Class)
     */
    @SuppressWarnings("unused")
    public void deleteGlobalBean(Class<? extends GlobalBean> type) {
        globalBeanManager.deleteBean(type);
    }

    /**
     * Set the global bean manager instance
     * @see #setGlobalBeanManager(GlobalBeanManager)
     * @see #getGlobalBeanManager()
     */
    @SuppressWarnings("unused")
    public JWebServer withGlobalBeanManager(GlobalBeanManager beanManager) {
        setGlobalBeanManager(beanManager);
        return this;
    }

    /**
     * Set the global bean manager instance
     * @see #getGlobalBeanManager()
     * @see #withGlobalBeanManager(GlobalBeanManager)
     */
    @SuppressWarnings("unused")
    public void setGlobalBeanManager(GlobalBeanManager beanManager) {
        this.globalBeanManager = beanManager;
    }

    /**
     * Get the global bean manager instance
     * @see #setGlobalBeanManager(GlobalBeanManager)
     * @see #withGlobalBeanManager(GlobalBeanManager)
     */
    @SuppressWarnings("unused")
    public GlobalBeanManager getGlobalBeanManager() {
        return globalBeanManager;
    }

    /**
     * Remove a registered handler
     * @param uri same uri as the one specified whilst registering handler, usually using the @URI annotation
     */
    @SuppressWarnings("unused")
    public void removeHandler(String uri){
        for(URIObject obj : methods) {
            if(obj.uri().equals(uri)) {
                MethodHandler handler = methods.get(obj);
                logger.finest(String.format("Remove Method > %s:%s %s %s", handler.getMethod().getDeclaringClass().getName(), handler.getMethod().getName(), obj.uri(), Arrays.toString(obj.methods())));
                methods.remove(obj);
                break;
            }
        }
    }

    /**
     * Set the executor used by the server.
     * @see #setExecutor(ScheduledThreadPoolExecutor)
     * @see #getExecutor()
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public ScheduledThreadPoolExecutor getExecutor() {
        return this.executor;
    }

    /**
     * Set the logger instance used by this server
     * @see #setLogger(Logger)
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
     * Automatically loads and registers all classes annotated with the {@link com.bethibande.web.annotations.AutoLoad} annotation.
     * This only loads classes where the @AutoLoad annotations value is equals the specified module parameter.
     * Works for handlers, parameter processors and method invocation handlers.
     * @param module The value supplied to the @AutoLoad annotation, the annotations value has to be equals to this parameter for the class to be loaded
     * @param root Can be any class that is being loaded from the same location as the classes you want to load.
     *             A location can be anything, a jar file, a directory or more.
     *             A location is being loaded like this -> class.getProtectionDomain().getCodeSource().getLocation()
     */
    @SuppressWarnings({"unused","unchecked"})
    public JWebServer autoLoad(Class<?> root, String module) {
        ClassCollector collector = new ClassCollector();
        Collection<Class<?>> classes = collector.collect(root, module);

        for(Class<?> clazz : classes) {
            if(ParameterProcessor.class.isAssignableFrom(clazz)) {
                registerProcessor(ReflectUtils.autoWireNewInstance((Class<? extends ParameterProcessor>) clazz, new ServerContext(this, null, null)));
                continue;
            }
            if(MethodInvocationHandler.class.isAssignableFrom(clazz)) {
                registerMethodInvocationHandler(ReflectUtils.autoWireNewInstance((Class<? extends MethodInvocationHandler>) clazz, new ServerContext(this, null, null)));
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
    @SuppressWarnings("unchecked")
    public JWebServer autoLoad(Class<?> root) {
        ClassCollector collector = new ClassCollector();
        Collection<Class<?>> classes = collector.collect(root, AutoLoad.class);

        for(Class<?> clazz : classes) {
            if(ParameterProcessor.class.isAssignableFrom(clazz)) {
                registerProcessor(ReflectUtils.autoWireNewInstance((Class<? extends ParameterProcessor>) clazz, new ServerContext(this, null, null)));
                continue;
            }
            if(MethodInvocationHandler.class.isAssignableFrom(clazz)) {
                registerMethodInvocationHandler(ReflectUtils.autoWireNewInstance((Class<? extends MethodInvocationHandler>) clazz, new ServerContext(this, null, null)));
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public JWebServer withWriter(Class<?> type, Class<? extends OutputWriter> writer) {
        registerWriter(type, writer);
        return this;
    }

    public void registerWriter(Class<?> type, Class<? extends OutputWriter> writer) {
        logger.config(String.format("Register Writer > %s for type %s", writer.getName(), type.getName()));
        writers.remove(type);
        writers.put(type, writer);
    }

    public JWebServer withHandler(Class<?> handler) {
        registerHandlerClass(handler);
        return this;
    }

    @SuppressWarnings("unused")
    public JWebServer withProcessor(ParameterProcessor processor) {
        registerProcessor(processor);
        return this;
    }

    @SuppressWarnings("unused")
    public <T> JWebServer withOutputHandler(Class<T> type, OutputHandler<T> handler) {
        registerOutputHandler(type, handler);
        return this;
    }

    public <T> void registerOutputHandler(Class<T> type, OutputHandler<T> handler) {
        logger.config(String.format("Register OutputHandler > %s for type %s", handler.getClass().getName(), type.getName()));
        outputHandlers.put(type, handler);
    }

    @SuppressWarnings("unchecked")
    public <T> OutputHandler<T> getOutputHandler(Class<T> type) {
        return (OutputHandler<T>) outputHandlers.get(type);
    }

    @SuppressWarnings("unused")
    public HashMap<Class<?>, OutputHandler<?>> getOutputHandlers() {
        return outputHandlers;
    }

    public void registerProcessor(ParameterProcessor processor) {
        logger.config(String.format("Register ParameterProcessor > %s", processor.getClass().getName()));
        processors.add(processor);
    }

    @SuppressWarnings("unused")
    public JWebServer withMethod(Class<?> clazz, String methodName, Class<?>... methodSignature) {
        registerMethod(clazz, methodName, methodSignature);
        return this;
    }

    @SuppressWarnings("unused")
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
        final URI uri = method.getAnnotation(URI.class);
        final ProcessorMappings mappings = ProcessorMappings.of(method, this);

        logger.finest(String.format("Register Method > %s:%s %s %s", method.getDeclaringClass().getName(), method.getName(), uri.value(), Arrays.toString(uri.methods())));

        final MethodHandler methodHandler;
        if(Modifier.isStatic(method.getModifiers())) {
            methodHandler = new StaticMethodHandler(method, mappings);
        } else {
            methodHandler = new InstanceMethodHandler(method, mappings);
        }

        this.methods.searchInsert(URIObject::priority, URIObject.of(uri), methodHandler);
    }

    public void registerHandlerClass(Class<?> handler) {
        logger.config(String.format("Register Class > %s", handler.getName()));
        for(Method method : handler.getDeclaredMethods()) {
            if(!method.isAnnotationPresent(URI.class)) continue;

            registerMethod(method);
        }
    }

    @SuppressWarnings("unused")
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

    public boolean isAlive() {
        return !interfaces.isEmpty();
    }

    /**
     * Gets all server interfaces the server is listening on
     */
    public List<ServerInterface> getInterfaces() {
        return List.copyOf(interfaces);
    }

    /**
     * Get a server interface using its bind address. The method will return null,
     * if there is no interface for the given address or the address is null.
     */
    public @Nullable ServerInterface getInterfaceByAddress(final InetSocketAddress address) {
        if(address == null) return null;

        return interfaces.stream().filter(it -> it.address().equals(address)).findFirst().orElse(null);
    }

    /**
     * Stops and deletes a server interface. <br>
     * !! Note: Memory Leaks, if you want to stop all interfaces, and don't plan to reuse the server instance,
     * call {@link #stop()}. stop() will clear the internal session and request cache.
     * This method however does not clear internal caches.
     */
    public void stop(final ServerInterface _interface) {
        _interface.server().stop(0);
        interfaces.remove(_interface);
        logger.info(String.format(
                "%s stopped %s",
                annotate("JWebAPI Interface", BLUE + BOLD),
                annotate(_interface.server().getAddress().toString().substring(1), MAGENTA)
        ));
    }


    /**
     * Stops the entire server including all server interfaces and destroys session and global request caches,
     * allowing them to be collected by garbage collection.
     */
    public void stop() {
        interfaces.forEach(this::stop);

        interfaces.clear();

        this.sessionCache = null;
        this.globalRequestCache = null;

        logger.info(String.format("%s stopped", annotate("JWebAPI Server", BLUE + BOLD)));
    }

    /**
     * Start a new server interface using the given bindAddress. The JWebServer instance will start listening to incoming
     * connections on the given bind address and port.
     * The resulting interface can be retrieved using {@link #getInterfaceByAddress(InetSocketAddress)} and stopped using
     * {@link #stop(ServerInterface)} or by stopping all interfaces using {@link #stop()}
     * @param bindAddress address to bind to
     */
    public void start(final InetSocketAddress bindAddress) {
        try {
            HttpServer server = new com.bethibande.web.tcp.HttpServer();
            server.bind(bindAddress, 100);
            start(server);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This will wrap the given HttpServer instance in a ServerInterface and start listening for incoming
     * connections/requests on the given server.
     * The resulting interface can be retrieved using {@link #getInterfaceByAddress(InetSocketAddress)} and stopped using
     * {@link #stop(ServerInterface)} or by stopping all interfaces using {@link #stop()}
     * @param server server to wrap
     */
    public void start(final HttpServer server) {
        this.interfaces.add(new ServerInterface(this, server.getAddress(), server));
        server.setExecutor(executor);
        server.start();

        if(this.sessionCache == null) {
            this.sessionCache = cacheSupplier.getSessionCache(
                    this,
                    cacheConfig
            );
        }

        if(this.globalRequestCache == null) {
            this.globalRequestCache = cacheSupplier.getRequestCache(
                    this,
                    cacheConfig,
                    CacheType.GLOBAL_REQUEST_CACHE
            );
        }

        server.createContext("/", new HttpHandler(this));

        logger.info(String.format("%s started on %s", annotate("JWebAPI Interface", BLUE + BOLD), annotate(server.getAddress().toString().substring(1), MAGENTA)));
    }

}

