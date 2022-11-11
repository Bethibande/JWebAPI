package com.bethibande.web.beans;

import com.bethibande.web.context.ServerContext;

public interface BeanFactory {

    <T extends Bean> T create(Class<T> type, ServerContext context);

}
