package com.bethibande.web.examples.chat;

import com.bethibande.web.JWebServer;
import com.bethibande.web.examples.chat.annotations.AuthRequiredHandler;
import com.bethibande.web.examples.chat.context.ChatContext;
import com.bethibande.web.examples.chat.handlers.ChatServerHandler;
import com.bethibande.web.examples.chat.types.ChatMessage;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class ChatServer {

    public static final int PORT = 34564;

    public static final AtomicLong messageCounter = new AtomicLong();

    public static final List<String> names = new ArrayList<>();
    public static final List<ChatMessage> messages = new ArrayList<>();

    public static void main(String[] args) {
        JWebServer server = new JWebServer()
                .withHandler(ChatServerHandler.class)
                .withMethodInvocationHandler(new AuthRequiredHandler())
                .withContextFactory(ChatContext::new);
        server.start(new InetSocketAddress("127.0.0.1", PORT));

        names.add("system");
    }

    public static void createMessage(final String name, final String text) {
        final ChatMessage message = new ChatMessage(messageCounter.getAndIncrement(), System.currentTimeMillis(), name, text);
        messages.add(message);
        if(messages.size() > 100) messages.remove(0);
    }

}
