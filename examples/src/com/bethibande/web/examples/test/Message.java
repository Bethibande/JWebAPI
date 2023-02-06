package com.bethibande.web.examples.test;

@SuppressWarnings("unused")
public record Message(int id, String message) {

    public enum MessageType {
        HELLO_WORLD(1, "Hello World!"),
        TEST(2, "Test URI"),
        OK(3, "OK"),
        NOT_FOUND(404, "Not Found"),
        ACCESS_DENIED(403, "Access denied");

        private final int id;
        private final String message;

        MessageType(int id, String message) {
            this.id = id;
            this.message = message;
        }

        public int getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }

        public Message toMessage() {
            return new Message(id, message);
        }
    }
}
