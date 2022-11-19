package com.bethibande.web.examples;

import com.bethibande.web.annotations.AutoLoad;
import com.bethibande.web.loader.ClassCollector;

import java.util.Collection;

public class LoadTest {

    public static void main(String[] args) {
        ClassCollector collector = new ClassCollector();
        Collection<Class<?>> classes = collector.collect(LoadTest.class, AutoLoad.class);

        classes.forEach(c -> System.out.printf("found: %s%n", c.getName()));
    }

}
