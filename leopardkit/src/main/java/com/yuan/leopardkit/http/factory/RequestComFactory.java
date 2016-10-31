package com.yuan.leopardkit.http.factory;

import com.yuan.leopardkit.interfaces.HttpRespondResult;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Yuan on 2016/9/3.
 * Detail 拦截获取RESPONSE
 */

public class RequestComFactory implements Interceptor {

    private HttpRespondResult httpRespondResult;

    public RequestComFactory() {
    }

    public void setHttpRespondResult(HttpRespondResult httpRespondResult) {
        this.httpRespondResult = httpRespondResult;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        Request request = builder.build();
        if (request!=null && httpRespondResult!=null)
        httpRespondResult.setRequest(request);
        Response response = chain.proceed(request);
        if (response!=null && httpRespondResult!=null)
        httpRespondResult.setResponse(response);
        return response;
    }

    public static RequestComFactory create() {
        return new RequestComFactory();
    }
}
