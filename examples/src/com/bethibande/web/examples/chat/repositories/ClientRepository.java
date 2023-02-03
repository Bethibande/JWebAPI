package com.bethibande.web.examples.chat.repositories;

import com.bethibande.web.annotations.PostData;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.examples.chat.types.ChatMessage;
import com.bethibande.web.examples.chat.types.AuthResponse;
import com.bethibande.web.examples.chat.types.MessageType;
import com.bethibande.web.examples.chat.types.NameType;
import com.bethibande.web.types.RequestMethod;

import java.util.List;

public interface ClientRepository {

    @URI(value = "/auth", methods = RequestMethod.POST)
    AuthResponse authenticate(final @PostData NameType name);

    @URI(value = "/getMessages", methods = RequestMethod.GET)
    List<ChatMessage> getMessages();

    @URI(value = "/createMessage", methods = RequestMethod.POST)
    void sendMessage(final @PostData MessageType message);

    @URI(value = "/disconnect", methods = RequestMethod.POST)
    void disconnect(final @PostData NameType name);

}
