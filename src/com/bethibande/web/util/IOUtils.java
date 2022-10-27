package com.bethibande.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class IOUtils {

    public static String readString(InputStream stream, long _length, Charset charset, int bufferSize) {
        try {
            StringBuilder sb = new StringBuilder();

            final byte[] buffer = new byte[bufferSize];
            int read;
            long length = _length;
            while((read = stream.read(buffer)) > 0) {
                sb.append(new String(buffer, 0, read, charset));

                length -= read;
                if(length <= 0) break;
            }

            return sb.toString();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

}
