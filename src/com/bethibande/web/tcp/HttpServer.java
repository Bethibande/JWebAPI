package com.bethibande.web.tcp;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpServer extends com.sun.net.httpserver.HttpServer {

    private ServerSocket socket;
    private ThreadPoolExecutor executor;

    private final HashMap<String, HttpHandler> contexts = new HashMap<>();

    private volatile boolean stop = false;

    @Override
    public void bind(final InetSocketAddress addr, final int backlog) throws IOException {
        if(socket != null) return;
        socket = new ServerSocket(addr.getPort(), backlog, addr.getAddress());
    }

    private static final byte LINE_BREAK = 10;
    private static final byte CARRIAGE_RETURN = 13;

    private byte[] readHeader(InputStream in) throws IOException {
        byte[] header = new byte[2048];
        int length = 0;

        in.readNBytes(header, length, 14);
        length += 14;

        while(true) {
            in.readNBytes(header, length, 1);
            length++;

            if(length < 4) continue;

            if(header[length - 1] == LINE_BREAK && header[length - 2] == CARRIAGE_RETURN && header[length - 3] == LINE_BREAK && header[length - 4] == CARRIAGE_RETURN) break;
        }

        return Arrays.copyOf(header, length);
    }

    @SuppressWarnings("ConstantConditions")
    private void handleClient(Socket client) {
        try {
            InputStream in = client.getInputStream();

            byte[] header = readHeader(in);
            String headerStr = new String(header, StandardCharsets.UTF_8);
            String[] lines = headerStr.split("\r\n");

            String method = lines[0].split(" ")[0];
            String uri = lines[0].split(" ")[1];
            String version = lines[0].split(" ")[2];

            Headers headers = new Headers();


            boolean first = true;
            for(String line : lines) {
                if(first) {
                    first = false;
                    continue;
                }

                String[] split = line.split(": ");

                headers.add(split[0], split[1]);
            }

            HttpExchange exchange = new HttpExchange(client, method, URI.create(uri), version, headers);

            for(Map.Entry<String, HttpHandler> entry : contexts.entrySet()) {
                if(uri.toLowerCase().startsWith(Optional.ofNullable(entry.getKey()).map(String::toLowerCase).orElse(null))) {
                    entry.getValue().handle(exchange);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void accept() {
        try {
            Socket client = socket.accept();
            if(!stop) executor.execute(this::accept);

            this.handleClient(client);
        } catch(IOException ignored) {}
    }

    @Override
    public void start() {
        if(socket == null) throw new RuntimeException("Server has not yet been bound!");

        executor.execute(this::accept);
    }

    @Override
    public void setExecutor(Executor executor) {
        if(!(executor instanceof ThreadPoolExecutor tpe)) throw new RuntimeException("Executor has to be a ThreadPoolExecutor");
        this.executor = tpe;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    @SuppressWarnings("unused")
    public void stop(int delay) {
        stop = true;

        try {
            boolean result = executor.awaitTermination(delay, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpContext createContext(String path, HttpHandler handler) {
        contexts.put(path, handler);
        return null;
    }

    @Override
    public HttpContext createContext(String path) {
        return null;
    }

    @Override
    public void removeContext(String path) throws IllegalArgumentException {

    }

    @Override
    public void removeContext(HttpContext context) {

    }

    @Override
    public InetSocketAddress getAddress() {
        return (InetSocketAddress) socket.getLocalSocketAddress();
    }
}