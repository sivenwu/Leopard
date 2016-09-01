package com.yuan.leopardkit.interfaces;

import android.content.Context;

import cn.yuan.leopardkit.R;


/**
 * Created by Yuan on 2016/9/1.
 * Detail 依赖请求时回调
 */
public abstract class HttpRespondResult implements IHttpLoading {

    private Context mContext;
    private ILoading mLoadViewCallBack;

    private boolean isShowLoad = false;
    private String loadMessage;

    public HttpRespondResult() {

    }

    public HttpRespondResult(Context mContext) {
        this(mContext.getResources().getString(R.string.load_message),mContext);
    }

    public HttpRespondResult(String loadMessage, Context mContext) {
        this.loadMessage = loadMessage;
        this.mContext = mContext;
        isShowLoad = loadMessage.length()>0?true:false;

        try {
            mLoadViewCallBack = (ILoading) mContext;
        }catch (Exception e){
            mLoadViewCallBack = null;
        }
    }

    @Override
    public void onPreViewAction() {
        if (isShowLoad && (mLoadViewCallBack!=null)){
            mLoadViewCallBack.onStartLoading(this.loadMessage);
        }
    }

    @Override
    public void onAfterViewAction() {
        if (isShowLoad && (mLoadViewCallBack!=null)){
            mLoadViewCallBack.onFinshLoading();
        }
    }

    //向外抛出的两个方法
    public abstract void onSuccess(String content);
    public abstract void onFailure(Throwable error, String content);
}
