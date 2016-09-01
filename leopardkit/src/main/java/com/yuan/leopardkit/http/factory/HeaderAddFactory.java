package com.yuan.leopardkit.http.factory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Yuan on 2016/8/24.
 * Detail　依赖自定义添加头部时
 */
public class HeaderAddFactory implements Interceptor {

    private Map<String, String> headers = new HashMap<>();

    public HeaderAddFactory(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request.Builder builder = chain.request().newBuilder();
        for (String key : headers.keySet()) {
            builder.addHeader(key, headers.get(key));
        }
        Request request = builder.build();
        return chain.proceed(request);
    }
}
