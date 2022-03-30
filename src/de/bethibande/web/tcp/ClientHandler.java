package de.bethibande.web.tcp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import de.bethibande.web.JWebClient;
import de.bethibande.web.annotations.FieldName;
import de.bethibande.web.annotations.HeaderField;
import de.bethibande.web.annotations.JsonData;
import de.bethibande.web.annotations.QueryField;
import de.bethibande.web.handlers.ClientHandle;
import de.bethibande.web.handlers.ClientHandleManager;
import de.bethibande.web.handlers.HandleType;
import de.bethibande.web.response.ServerResponse;
import de.bethibande.web.response.StreamResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ClientHandler<T> implements InvocationHandler {

    private JWebClient<T> client;

    public void setClient(JWebClient<T> client) {
        this.client = client;
    }

    public JWebClient<T> getClient() {
        return client;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ClientHandleManager manager = this.client.getClientHandleManager();
        ClientHandle handle = manager.get(method);
        Gson gson = new Gson();

        if(handle == null) {
            System.err.println("[JWebAPI] error, no method handle found for method: " + method);
            return null;
        }

        //HashMap<String, String> query = new HashMap<>();
        String uri = this.client.getBaseUrl();
        if(uri.endsWith("/")) {
            uri = uri + (handle.getUri().startsWith("/") ? handle.getUri().substring(1): handle.getUri());
        } else uri = uri + (handle.getUri().startsWith("/") ? handle.getUri() : "/" + handle.getUri());

        JsonObject jobj = handle.getType() == HandleType.JSON ? new JsonObject(): null;
        byte[] postData = null;
        InputStream st = null;
        String contentType = null;
        long contentLength = 0;
        HashMap<String, String> headerFields = new HashMap<>();
        StringBuilder queryString = new StringBuilder("?");

        for(int i = 0; i < method.getParameterTypes().length; i++) {
            Class<?> t = method.getParameterTypes()[i];
            Parameter p = method.getParameters()[i];

            if(p.isAnnotationPresent(QueryField.class)) {
                QueryField f = p.getAnnotation(QueryField.class);
                String key = f.value();
                String def = f.def();
                Object val = args[i];

                if(val == null) val = def;

                //query.put(key, val.toString());
                if(val instanceof Boolean && (boolean) val) {
                    queryString.append(key + "&");
                    continue;
                }
                if(val instanceof Boolean) {
                    continue;
                }
                queryString.append(key + "=" + URLEncoder.encode(val.toString(), StandardCharsets.UTF_8) + "&");
                continue;
            }

            if(p.isAnnotationPresent(JsonData.class)) {
                String js = new Gson().toJson(args[i]);
                postData = js.getBytes(StandardCharsets.UTF_8);
                contentLength = postData.length;
                continue;
            }

            if(p.isAnnotationPresent(HeaderField.class)) {
                HeaderField f = p.getAnnotation(HeaderField.class);
                Object val = args[i];
                if(val == null) val = f.def();

                headerFields.put(f.value(), val.toString());
                continue;
            }

            if(t == StreamResponse.class) {
                StreamResponse sr = (StreamResponse) args[i];
                st = sr.getStream();
                contentLength = sr.getLength();
                contentType = sr.getContentType();
                continue;
            }

            if(jobj != null) {
                if(!p.isNamePresent()) {
                    if(!p.isAnnotationPresent(FieldName.class)) {
                        System.err.println("[JWebAPI] Found parameter for json mappings without @FieldName annotation and without name, please annotate field or enable field names in your compile config (javac -parameters option)");
                    }
                    jobj.add(p.getAnnotation(FieldName.class).value(), gson.toJsonTree(args[i]));
                    continue;
                }
                jobj.add(p.getName(), gson.toJsonTree(args[i]));
            }
        }

        queryString.delete(queryString.length() - 1, queryString.length());

        if(jobj != null) {
            String js = jobj.toString();
            postData = js.getBytes(StandardCharsets.UTF_8);
            contentLength = postData.length;
        }

        if(postData != null && st != null) {
            System.err.println("[JWebAPI] cannot send json data and stream in one request: " + method);
            return null;
        }

        String url = uri + (queryString.length() > 1 ? queryString: "");
        URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection)u.openConnection();
        if(contentLength > 0) {
            con.setRequestMethod("POST");
            con.setFixedLengthStreamingMode(contentLength);
        } else con.setRequestMethod("GET");

        if(postData != null) con.setRequestProperty("Content-Type", "text/json");
        if(st != null) con.setRequestProperty("Content-Type", contentType);

        for(String key : headerFields.keySet()) {
            String val = headerFields.get(key);
            con.setRequestProperty(key, val);
        }

        con.setDoOutput(true);
        con.connect();

        OutputStream out = con.getOutputStream();
        if(postData != null) {
            long readTotal = 0;
            int read;
            ByteArrayInputStream in = new ByteArrayInputStream(postData);
            byte[] buffer = new byte[this.client.getBufferSize()];
            while((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);

                readTotal += read;
                if(readTotal >= contentLength) break;
            }
        }
        if(st != null) {
            long readTotal = 0;
            int read;
            byte[] buffer = new byte[this.client.getBufferSize()];
            while((read = st.read(buffer)) > 0) {
                out.write(buffer, 0, read);

                readTotal += read;
                if(readTotal >= contentLength) break;
            }
        }

        contentLength = con.getContentLengthLong();
        InputStream in = con.getInputStream();

        if(contentLength <= 0) return null;

        if(method.getReturnType() == StreamResponse.class) {
            return ServerResponse.stream(in, con.getContentType(), contentLength);
        }

        ByteBuffer buff = ByteBuffer.allocate((int)contentLength);
        long readTotal = 0;
        int read;
        byte[] buffer = new byte[this.client.getBufferSize()];
        while((read = in.read(buffer)) > 0) {
            buff.put(buffer, 0, read);

            readTotal += read;
            if(readTotal >= contentLength) break;
        }

        return new Gson().fromJson(new String(buff.array(), StandardCharsets.UTF_8), method.getReturnType());
    }
}
