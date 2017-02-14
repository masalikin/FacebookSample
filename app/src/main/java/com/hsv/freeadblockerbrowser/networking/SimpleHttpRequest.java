package com.hsv.freeadblockerbrowser.networking;

import android.support.annotation.Nullable;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class SimpleHttpRequest<T> extends MyAsyncTask<T> {

    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private static final int DEFAULT_READ_TIMEOUT = 5000;

    private final Map<String, String> params = new HashMap<>();

    private String url;
    private Method method;
    private String postData;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    public SimpleHttpRequest<T> setUrl(String url) {
        this.url = url;
        return this;
    }

    public SimpleHttpRequest<T> setMethod(Method method) {
        this.method = method;
        return this;
    }

    public SimpleHttpRequest<T> setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public SimpleHttpRequest<T> setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public SimpleHttpRequest<T> withParam(String name, String value) {
        if (name != null && !name.isEmpty() && value != null && !value.isEmpty()) {
            params.put(name, value);
        }
        return this;
    }

    public SimpleHttpRequest<T> setPostData(String postData) {
        this.postData = postData;
        return this;
    }

    private String appendParams(String url) {
        if (params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(url).append("?");

        Set<String> keys = params.keySet();
        for (String key : keys) {
            String value = params.get(key);
            sb.append(key).append("=").append(value).append("&");
        }
        return sb.toString();
    }

    @Nullable
    protected T doInBackground() {
        HttpURLConnection c = null;
        BufferedReader reader = null;
        try {
            String urlWithParams = appendParams(url);
            c = (HttpURLConnection) new URL(urlWithParams).openConnection();
            c.setConnectTimeout(connectTimeout);
            c.setReadTimeout(readTimeout);
            c.setRequestMethod(method.toString());
            switch (method) {
                case GET:
                    int responseCode = c.getResponseCode();
                    onResponseCode(responseCode);
                    String responseMessage = c.getResponseMessage();
                    onResponseMessage(responseMessage);
                    InputStream is = c.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(is), 1024);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    reader.close();
                    String json = sb.toString();

                    return parseResult(json);
                case POST:
                    c.setDoOutput(true);
                    OutputStream os = c.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(URLEncoder.encode("data", "UTF-8"));
                    sb1.append("=");
                    sb1.append(URLEncoder.encode(postData, "UTF-8"));
                    writer.write(sb1.toString());
                    writer.flush();
                    writer.close();
                    os.close();
                    c.connect();
                    break;
            }


        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onResponseCode(int responseCode) {
    }

    protected void onResponseMessage(String responseMessage) {
    }

    protected T parseResult(@Nullable String response) throws Exception {
        return null;
    }

    public enum Method {
        GET,
        POST,
    }

}
