package com.yuan.leopardkit.http.base;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yuan on 2016/9/1.
 * Detail 基础的请求实体类
 */
public abstract class BaseEnetity {

    /**
     * 获取请求链接
     *
     * @return
     */
    public abstract String getRuqestURL();

    // 获得map类型
    public Map<String, Object> getMapEnticty() {
        Class<? extends BaseEnetity> clazz = this.getClass();
        Class<? extends Object> superclass = clazz.getSuperclass();

        Field[] fields = clazz.getDeclaredFields();
        Field[] superFields = superclass.getDeclaredFields();

        if (fields == null || fields.length == 0) {
            return Collections.emptyMap();
        }

        Map<String, Object> params = new HashMap<String, Object>();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                params.put(field.getName(), field.get(this));
            }

            for (Field superField : superFields) {
                superField.setAccessible(true);
                params.put(superField.getName(), superField.get(this));
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return params;
    }

}
