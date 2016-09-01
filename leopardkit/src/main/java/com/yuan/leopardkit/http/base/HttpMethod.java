package com.yuan.leopardkit.http.base;

/**
 * Created by Yuan on 2016/9/1.
 * Detail 自定义请求资源 url
 */
public enum HttpMethod {

    //默认是键值
    GET("GET"),
    POST("POST"),
    GET_JSON("GET_JSON"),
    POST_JSON("POST_JSON");

    private final String value;

    private HttpMethod(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }

}
