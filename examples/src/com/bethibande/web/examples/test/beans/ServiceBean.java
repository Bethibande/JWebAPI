package com.bethibande.web.examples.test.beans;

import com.bethibande.web.annotations.PostConstruct;
import com.bethibande.web.beans.GlobalBean;

public class ServiceBean extends GlobalBean {

    private String message;

    @PostConstruct
    public void init() {
        // Initialize things here
        message = "Number %d\n";
    }

    public void printNumber(int number) {
        System.out.printf(message, number);
    }

}
