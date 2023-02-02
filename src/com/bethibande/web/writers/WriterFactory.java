package com.bethibande.web.writers;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.types.RequestWriter;

public interface WriterFactory<T> {

    RequestWriter create(final T value, final JWebAPI api);

}
