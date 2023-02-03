package com.bethibande.web.examples.chat;

import com.bethibande.web.JWebClient;
import com.bethibande.web.examples.chat.repositories.ClientRepository;
import com.bethibande.web.examples.chat.types.AuthResponse;
import com.bethibande.web.examples.chat.types.ChatMessage;
import com.bethibande.web.examples.chat.types.MessageType;
import com.bethibande.web.examples.chat.types.NameType;
import com.bethibande.web.logging.BasicStyle;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.logging.Logger;

public class ChatClient {

    private static final List<ChatMessage> messages = new ArrayList<>();
    private static Logger logger;

    public static void main(String[] args) {
        final JWebClient client = new JWebClient().withBaseUrl("http://127.0.0.1:" + ChatServer.PORT);
        logger = client.getLogger();

        final ClientRepository repository = client.withRepository(ClientRepository.class);

        final Scanner scanner = new Scanner(System.in);

        String name = null;

        while(name == null) {
            logger.info("Enter Username:");
            final String temp = scanner.nextLine();
            final AuthResponse response = repository.authenticate(new NameType(temp));

            if(response.status()) {
                name = temp;
                logger.info(response.message());
            } else {
                logger.warning(response.message());
            }
        }

        // Start receiving messages in the background
        final Thread messageThread = new Thread(() -> receiveMessages(repository));
        messageThread.setName("MessageReceiver");
        messageThread.setDaemon(true);
        messageThread.start();

        logger.info("Enter text and press enter to send or write 'exit' and press enter to exit");

        while(true) {
            final String str = scanner.nextLine();

            if(str.equalsIgnoreCase("exit")) {
                repository.disconnect(new NameType(name));
                logger.info("Good Bye!");
                break;
            }

            repository.sendMessage(new MessageType(name, str));
        }

        client.destroy();
    }

    public static void receiveMessages(final ClientRepository repository) {
        while(true) {
            final long minId = ChatClient.messages.isEmpty() ? -1L: ChatClient.messages.get(ChatClient.messages.size()-1).id();
            final List<ChatMessage> messages = repository.getMessages()
                                                         .stream()
                                                         .filter(message -> message.id() > minId)
                                                         .toList();

            messages.forEach(message -> {
                final LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(
                        message.date()),
                        TimeZone.getDefault().toZoneId()
                );
                logger.info(String.format(
                        "%s [%s] > %s",
                        BasicStyle.FORMATTER.format(date),
                        message.name(),
                        message.message()
                ));
            });

            ChatClient.messages.addAll(messages);
            if(ChatClient.messages.size() > 100) {
              final int size = ChatClient.messages.size();

                ChatClient.messages.subList(0, size).clear();
            }

            try {
                Thread.sleep(500);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
