package com.yuan.leopardkit.utils;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Yuan on 2016/9/1.
 *  json model 转化工具
 */
public class JsonParseUtil {

    private static final String LIST_TAG = "list";
    private static final String OBJ_TAG = "obj";
    private static Gson gson = new Gson();

    /**
     * 实体转化为json
     *
     * @param bean
     * @return
     */
    public static <T> String modeToJson(T bean) {
        return gson.toJson(bean);
    }

    /**
     * json转换为实体
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T jsonToMode(String json, Class<T> cls) {
        return gson.fromJson(json, cls);
    }

    /**
     * json转换为List<T>
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonArrayToList(String json, Class<T> cls) {
        Type type = new TypeToken<ArrayList<T>>() {
        }.getType();
        return gson.fromJson(json, type);
    }


}
