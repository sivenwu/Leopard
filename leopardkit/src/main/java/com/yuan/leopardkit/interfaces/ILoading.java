package com.yuan.leopardkit.interfaces;

/**
 * Created by Yuan on 2016/9/1.
 * Detail 依赖于界面，用于请求时
 */
public interface ILoading {

    /**
     * 开始加载回调
     * @param message
     */
    public void onStartLoading(String message);

    /**
     * 结束加载回调
     */
    public void onFinshLoading();

}
