package com.yuan.leopardkit.models;

/**
 * Created by Yuan on 2016/10/31.
 * Detail bean for cache
 */

public class CacheModel {

    private int type;//0 标记在线缓存 1标记离线缓存
    private byte[] headers;
    private byte[] responseData;

    public CacheModel() {
    }

    public CacheModel(byte[] headers, byte[] responseData) {
        this.headers = headers;
        this.responseData = responseData;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getHeaders() {
        return headers;
    }

    public void setHeaders(byte[] headers) {
        this.headers = headers;
    }

    public byte[] getResponseData() {
        return responseData;
    }

    public void setResponseData(byte[] responseData) {
        this.responseData = responseData;
    }
}
