# JWebAPI 1.1.1
A simple framework for easy creation of simple http servers/clients<br>

## Overview
- [Download](#download)
- [Dependencies](#dependencies)
- [Example](#example)

## Download
Download latest build [here](https://github.com/Bethibande/maven-repos/blob/main/de/bethibande/jwebapi/1.1.1/jwebapi-1.1.1.jar)
### Gradle
```gradle
repositories {
    mavenCentral()

    maven { url "https://github.com/Bethibande/maven-repos/raw/main" }
}

dependencies {
    implementation 'de.bethibande:jwebapi:1.1.1'
}
```
### Maven
```xml
<repository>
    <id>de.bethibande</id>
    <url>https://github.com/Bethibande/maven-repos/raw/main</url>
</repository>

<dependency>
    <groupId>de.bethibande</groupId>
    <artifactId>jwebapi</artifactId>
    <version>1.1.1</version>
</dependency>
```

## Dependencies
- Google Gson

## Example
Full example [here](https://github.com/Bethibande/JWebAPI/tree/master/examples/src/com/bethibande/web/examples)

### Server
Create the server
```java
JWebServer server = JWebServer.of(5566);
server.registerHandler(TestHandler.class);
server.start();
```
TestHandler.java
```java
@URI("/api")
public class TestHandler implements WebHandler {
    @URI("/test") // url = http://127.0.0.1:5566/api/test
    public static Object test(@QueryField(value = "name", def = "World") String name, @QueryField(value = "id", def = "1") int id) {
        return new Message(id, "Hello " + name + "!");
    }
    @URI("/") // uri = /api/, redirect if called http://ADDRESS/api to http://ADDRESS/api/test
    public static Object test2() {
        return ServerResponse.redirect("/api/test");
    }
}
```
Message.java
```java
public class Message {
    private int id;
    private String message;
    
    public Message(int id, String message) {
        this.id = id;
        this.message = message;
    }
    
    public String getMessage() { return this.message; }
    public int getId() { return this.id; }
}
```

### Client
Create client instance
```java
JWebClient<ApiClient> client = JWebClient.of(ApiClient.class, "http://127.0.0.1:5566");
ApiClient db = client.getInstance();

Message msg = db.getTestMessage("Max");
System.out.println(msg.getId() + ": " + msg.getMessage());
```
ApiClient.java
```java
public interface ApiClient {
    @URI("/api/test")
    Message getTestMessage(@QueryField(value = "name", def = "World") String name);
}
```
