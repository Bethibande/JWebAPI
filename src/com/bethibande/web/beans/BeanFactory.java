package com.bethibande.web.beans;

import com.bethibande.web.context.ServerContext;

public interface BeanFactory {

    <T> T create(Class<T> type, ServerContext context);

}
