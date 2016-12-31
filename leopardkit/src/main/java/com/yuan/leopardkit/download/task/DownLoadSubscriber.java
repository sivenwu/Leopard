package com.yuan.leopardkit.download.task;

import android.util.Log;

import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.interfaces.FileRespondResult;

import okhttp3.ResponseBody;
import rx.Subscriber;

/**
 * Created by Yuan on 2016/8/26.
 * Detail 下载订阅者
 */
public class DownLoadSubscriber<T extends ResponseBody> extends Subscriber<T> {

    private final String TAG = "DownLoadSubscriber";

    private FileRespondResult callback;
    private DownloadInfo downloadInfo;
    private DownLoadTask task;

    public DownLoadSubscriber(FileRespondResult callback, DownloadInfo downloadInfo, DownLoadTask task) {
        this.callback = callback;
        this.downloadInfo = downloadInfo;
        this.downloadInfo.setSubscriber(this);
        this.task = task;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onCompleted() {
        Log.i(TAG, "onCompleted");
    }

    @Override
    public void onError(Throwable e) {
        Log.i(TAG, "onError : "+e.getMessage());
    }

    @Override
    public void onNext(T responseBody) {
//        if (this.downloadInfo.getState() != DownLoadManager.STATE_PAUSE)
        task.downFinsh();
    }
}
