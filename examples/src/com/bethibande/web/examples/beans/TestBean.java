package com.bethibande.web.examples.beans;

import com.bethibande.web.annotations.Path;
import com.bethibande.web.annotations.PostConstruct;
import com.bethibande.web.annotations.PostDestroy;
import com.bethibande.web.beans.Bean;

@SuppressWarnings("unused")
public class TestBean extends Bean {

    private int number;
    private final transient String path;
    private final transient ServiceBean serviceBean;

    public TestBean(@Path String path, ServiceBean bean) {
        this.path = path;
        this.serviceBean = bean;
    }

    public int getNumber() {
        return number;
    }

    public String getPath() {
        return path;
    }

    public void increment() {
        number++;
        serviceBean.printNumber(number);
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
