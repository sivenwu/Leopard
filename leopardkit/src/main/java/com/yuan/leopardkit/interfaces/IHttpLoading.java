package com.yuan.leopardkit.interfaces;

/**
 * Created by Yuan on 2016/9/1.
 * Detail 依赖于http请求时监听
 */
public interface IHttpLoading {

    /**
     * HTTP开始请求回调
     */
    public void onPreViewAction();

    /**
     * HTTP响应完成回调
     */
    public void onAfterViewAction();

}
