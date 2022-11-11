package com.bethibande.web.examples;

import com.bethibande.web.beans.BeanManager;
import com.bethibande.web.examples.beans.TestBean;

public class BeanTest {

    public static void main(String[] args) {
        BeanManager manager = new BeanManager();
        TestBean bean = manager.getBean(TestBean.class, null); // creates and gets bean
        bean.increment();
        System.out.println(bean.getNumber());

        manager.storeBean(bean);
        TestBean bean2 = manager.getBean(TestBean.class, null); // gets stored bean
        System.out.println(bean2.getNumber());
    }

}
