package com.bethibande.web.examples.beans;

import com.bethibande.web.annotations.PostConstruct;
import com.bethibande.web.annotations.PostDestroy;
import com.bethibande.web.annotations.WebBean;

@WebBean
public class TestBean {

    private int number;

    public int getNumber() {
        return number;
    }

    public void increment() {
        number++;
    }

    @PostConstruct
    public void onInit() {
        number = 0;
    }

    @PostDestroy
    public void onDestroy() {
        System.out.println("Destroy");
    }

}
