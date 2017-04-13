package com.yuan.leopardkit.interfaces;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.http.base.Task;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by siven on 17/3/31.
 */

public abstract class HttpRespondObjectResult<T>  extends HttpRespondResult{

    private Task<T> resultTask;

    public HttpRespondObjectResult() {
        Type type = getSuperclassTypeParameter(getClass(),0);

        this.resultTask = new Task(type);
    }

    public void addTask(String json) {
        resultTask.setJson(json);
        onSuccess(resultTask);
    }

    @Override
    public void onSuccess(String content) {
        //...
    }

    public abstract void onSuccess(Task<T> task);

    // 获取超类类型
    private Type getSuperclassTypeParameter(Class<?> clazz,int index)
    {
        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }

        return (Class) params[index];
    }

}
