package com.yuan.leopardkit.http.factory;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Yuan on 2016/8/24.
 * Detail 支持文件上传header
 */
public class UploadFileFactory implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .addHeader("Content-Type", "multipart/form-data")
                .build();
        return chain.proceed(request);
    }

    public static UploadFileFactory create(){
        return new UploadFileFactory();
    }
}
