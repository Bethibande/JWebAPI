package com.bethibande.web.types;

import java.io.OutputStream;

public interface RequestWriter {

    /**
     * The length in bytes of the data this writer is going to write
     * @return length in bytes n, n >= 0
     */
    long getLength();

    /**
     * Reset write offset to 0
     */
    void reset();

    /**
     * @return true if there is more data to write, may never write more than specified by {@link #getLength()}
     */
    boolean hasNext();

    /**
     * Write data to the given OutputStream. Writer should not write more data than the bufferSize allows.
     */
    void write(final OutputStream stream, final int bufferSize);

}
