package com.yuan.leopardkit.interfaces;

/**
 * Created by Yuan on 2017/3/1.
 * Detail 下载回调
 */

public interface IDownloadProgress {

    public void onProgress(long key,long progress, long total, boolean done);

    public void onSucess(String result);

    public void onFailed(Throwable e, String reason);

}
