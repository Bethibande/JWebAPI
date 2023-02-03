package com.bethibande.web.examples.chat.handlers;

import com.bethibande.web.annotations.JsonField;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.examples.chat.types.ChatMessage;
import com.bethibande.web.examples.chat.ChatServer;
import com.bethibande.web.examples.chat.annotations.AuthRequired;
import com.bethibande.web.examples.chat.context.ChatContext;
import com.bethibande.web.examples.chat.types.AuthResponse;
import com.bethibande.web.types.RequestMethod;

import java.util.List;

public class ChatServerHandler {

    @URI(value = "/auth", methods = RequestMethod.POST)
    public AuthResponse authenticate(@JsonField("name") final String username, final ChatContext context) {
        if(username == null) return new AuthResponse(false, "Invalid username");
        if(ChatServer.names.contains(username.toLowerCase())) {
            return new AuthResponse(false, "Username already in use.");
        }

        ChatServer.names.add(username.toLowerCase());
        context.addName(username);

        ChatServer.createMessage("System", "User connected: " + username);

        return new AuthResponse(true, "Welcome!");
    }

    @URI("/getMessages")
    @AuthRequired(simple = true)
    public List<ChatMessage> getMessages() {
        return ChatServer.messages;
    }

    @URI(value = "/createMessage", methods = RequestMethod.POST)
    @AuthRequired
    public void sendMessage(final @JsonField("name") String name, final @JsonField("message") String message) {
        ChatServer.createMessage(name, message);
    }

    @URI(value = "/disconnect", methods = RequestMethod.POST)
    @AuthRequired
    public void disconnect(final @JsonField("name") String name, final ChatContext context) {
        context.removeName(name);
        ChatServer.names.remove(name.toLowerCase());
    }

}
