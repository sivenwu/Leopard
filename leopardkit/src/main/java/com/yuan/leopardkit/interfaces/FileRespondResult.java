package com.yuan.leopardkit.interfaces;

import android.content.Context;

/**
 * Created by Yuan on 2016/8/25.
 * Detail 提供上传或者下载文件的回调
 */
public abstract class FileRespondResult extends HttpRespondResult{


    public FileRespondResult() {
    }

    public FileRespondResult(Context mContext) {
        super(mContext);
    }

    public FileRespondResult(String loadMessage, Context mContext) {
        super(loadMessage, mContext);
    }

    @Override
    public void onSuccess(String content) {

    }

    @Override
    public void onFailure(Throwable error, String content) {

    }

    // TODO: 2016/8/25 这里回调的时候不是主线程喔，所以必须通过信息机制或者其他通知给主线程更新UI
    public abstract void onExecuting(long progress, long total, boolean done);

    public abstract void onFailed(String reason);

}
