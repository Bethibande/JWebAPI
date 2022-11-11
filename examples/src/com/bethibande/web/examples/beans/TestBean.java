package com.bethibande.web.examples.beans;

import com.bethibande.web.annotations.Path;
import com.bethibande.web.annotations.PostConstruct;
import com.bethibande.web.annotations.PostDestroy;
import com.bethibande.web.beans.Bean;

public class TestBean extends Bean {

    private int number;
    private transient String path;

    public TestBean(@Path String path) {
        this.path = path;
    }

    public int getNumber() {
        return number;
    }

    public String getPath() {
        return path;
    }

    public void increment() {
        number++;
    }

    @PostConstruct
    public void onInit() {
        number = 1;
    }

    @PostDestroy
    public void onDestroy() {
        System.out.println("Destroy");
    }

}
