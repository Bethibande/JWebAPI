# JWebAPI 2-rc1
A simple framework for easy creation of simple http servers/clients<br>
<br>
### Please note that the current v2 version is not yet stable, code of the 1.1.1 version can be found [here](https://github.com/Bethibande/JWebAPI/tree/v1.1.1)

## Overview
- [Requirements](#requirements)
- [Download](#download)
- [Dependencies](#dependencies)
- [Example](#example)

## Requirements
### version 2 and later
- java 17
- [Dependencies](#dependencies)
### version 1.1.1 and below
- java 11
- [Google Gson](https://mvnrepository.com/artifact/com.google.code.gson/gson)

## Download
Download latest build [here](https://github.com/Bethibande/maven-repos/blob/main/JWebAPI.jar)
### Gradle
```gradle
repositories {
    mavenCentral()

    maven { url "https://github.com/Bethibande/maven-repos/raw/main" }
}

dependencies {
    implementation 'de.bethibande:jwebapi:2-rc1'
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
    <version>2-rc1</version>
</dependency>
```

## Dependencies
- [Google Gson](https://mvnrepository.com/artifact/com.google.code.gson/gson)
- [Jetbrains Annotations](https://mvnrepository.com/artifact/org.jetbrains/annotations)

## Example
Full example [here](https://github.com/Bethibande/JWebAPI/tree/master/examples/src/com/bethibande/web/examples)
