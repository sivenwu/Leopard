package com.yuan.leopardkit.http.base;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import com.yuan.leopardkit.utils.JsonParseUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 管理回掉后任务
 * Created by siven on 17/3/31.
 */

public class Task<T> {

    private String json;
    private T result;

    private Type clzType;

    public Task(Type type) {
        this.clzType = type;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
        result = new Gson().fromJson(json,clzType);
    }

    public T getResult() {
        return result;
    }
}
