package com.yuan.leopardkit.http.factory;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Yuan on 2016/8/23.
 * Detail 如果请求为json格式，必须添加这个factroy
 */
public class RequestJsonFactory implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        return chain.proceed(request);
    }

    public static RequestJsonFactory create(){
        return new RequestJsonFactory();
    }
}
