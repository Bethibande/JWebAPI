package de.bethibande.web;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import de.bethibande.web.annotations.*;
import de.bethibande.web.handlers.HandleType;
import de.bethibande.web.handlers.MethodHandle;
import de.bethibande.web.reflect.ClassUtils;
import de.bethibande.web.response.ServerResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class StandardRequestProcessor implements RequestProcessor {

    private JWebServer server;

    public StandardRequestProcessor(JWebServer server) {
        this.server = server;
    }

    public Object invoke(MethodHandle handle, Object instance, Object[] values) {
        try {
            return handle.getMethod().invoke(handle.isStatic() ? null: instance, values);
        } catch(IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ByteBuffer readAll(InputStream in, int length) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(length);
        byte[] buffer = new byte[this.server.getBufferSize()];
        int read, readTotal = 0;
        while((read = in.read(buffer)) > 0) {
            buff.put(buffer, 0, read);

            readTotal += read;
            if(readTotal >= length) break;
        }

        return buff;
    }


    @Override
    public Object processRequest(InetSocketAddress sender, String uri, String method, InputStream in, Object instance, MethodHandle handle, HashMap<String, String> query, Headers headers) throws IOException {
        Method m = handle.getMethod();
        Object[] values = new Object[m.getParameterTypes().length];
        String js = null;
        JsonObject jobj = null;

        if(m.getParameterTypes().length == 0) {
            return invoke(handle, instance, null);
        }

        if(handle.getInputType() == HandleType.JSON) {
            if(!headers.containsKey("Content-Length") || headers.getFirst("Content-Length").equals("0")) return ServerResponse.httpStatusCode(405);
            int contentLength = Integer.parseInt(headers.getFirst("Content-Length"));
            js = new String(readAll(in, contentLength).array(), this.server.getCharset());
            jobj = new Gson().fromJson(js, JsonObject.class);
        }

        for(int i = 0; i < m.getParameterTypes().length; i++) {
            Class<?> t = m.getParameterTypes()[i];
            Parameter p = m.getParameters()[i];

            if(p.isAnnotationPresent(QueryField.class)) {
                QueryField f = p.getAnnotation(QueryField.class);
                String val;
                if(query.containsKey(f.value().toLowerCase())) {
                    val = query.get(f.value().toLowerCase());
                } else val = f.def();

                Object obj = null;
                if(t == String.class) obj = val;
                if(t == Byte.class || t == byte.class) obj = Byte.parseByte(val);
                if(t == Short.class || t == short.class) obj = Short.parseShort(val);
                if(t == Integer.class || t == int.class) obj = Integer.parseInt(val);
                if(t == Long.class || t == long.class) obj = Long.parseLong(val);
                if(t == Double.class || t == double.class) obj = Double.parseDouble(val);
                if(t == Float.class || t == float.class) obj = Float.parseFloat(val);

                if(t == Boolean.class || t == boolean.class) {
                    if(val == null) obj = true;
                    if(val.equals("true")) obj = true;
                    if(!val.equals("true")) obj = false;
                }

                values[i] = obj;
                continue;
            }

            if(p.isAnnotationPresent(JsonData.class) && handle.getInputType() == HandleType.DEFAULT) {
                if(!headers.containsKey("Content-Length") || headers.getFirst("Content-Length").equals("0")) return ServerResponse.httpStatusCode(405);
                int contentLength = Integer.parseInt(headers.getFirst("Content-Length"));

                ByteBuffer buff = js == null ? readAll(in, contentLength): null;

                String json = js == null ? new String(buff.array(), this.server.getCharset()): js;
                Object obj = new Gson().fromJson(json, t);

                values[i] = obj;
                continue;
            } else if(handle.getInputType() == HandleType.STREAM) {
                System.err.println("[JWebAPI] Invalid method handle, @JsonData and InputStream parameter in one method");
            }

            if(p.isAnnotationPresent(HeaderField.class)) {
                HeaderField h = p.getAnnotation(HeaderField.class);
                String val;
                if(headers.containsKey(h.value())) {
                    val = headers.getFirst(h.value());
                } else val = h.def();

                Object obj = null;
                if(t == String.class) obj = val;
                if(t == Byte.class || t == byte.class) obj = Byte.parseByte(val);
                if(t == Short.class || t == short.class) obj = Short.parseShort(val);
                if(t == Integer.class || t == int.class) obj = Integer.parseInt(val);
                if(t == Long.class || t == long.class) obj = Long.parseLong(val);
                if(t == Double.class || t == double.class) obj = Double.parseDouble(val);
                if(t == Float.class || t == float.class) obj = Float.parseFloat(val);

                if(t == Boolean.class || t == boolean.class) {
                    if(val == null) obj = true;
                    if(val.equals("true")) obj = true;
                    if(!val.equals("true")) obj = false;
                }

                values[i] = obj;
                continue;
            }

            if(p.isAnnotationPresent(ContentLength.class)) {
                if(!(t == int.class || t == Integer.class)) {
                    System.err.println("[JWebAPI] @ContentLength may only annotate parameters of the type int/Integer");
                    continue;
                }

                String val;
                if(headers.containsKey("Content-Length")) {
                    val = headers.getFirst("Content-Length");
                } else val = "0";

                values[i] = Integer.parseInt(val);
                continue;
            }

            if(p.isAnnotationPresent(HttpType.class)) {
                if(t != String.class) {
                    System.err.println("[JWebAPI] @HttpType may only annotate parameters of the type String");
                    continue;
                }

                String val;
                if(headers.containsKey("Content-Type")) {
                    val = headers.getFirst("Content-Type");
                } else val = "text/plain";

                values[i] = val;
                continue;
            }

            if(t == InputStream.class) {
                values[i] = in;
                continue;
            }

            if(jobj != null) {
                if(p.isNamePresent() && jobj.has(p.getName())) {
                    JsonElement el = jobj.get(p.getName());
                    values[i] = new Gson().fromJson(el, t);
                    continue;
                }
                if(p.isAnnotationPresent(FieldName.class)) {
                    String name = p.getAnnotation(FieldName.class).value();
                    if(jobj.has(name)) {
                        JsonElement el = jobj.get(p.getAnnotation(FieldName.class).value());
                        values[i] = new Gson().fromJson(el, t);
                        continue;
                    }
                }
            }

            values[i] = ClassUtils.generateDefaultValue(t); // either null, false or 0, depending on the type
        }

        return invoke(handle, instance, values);
    }
}
