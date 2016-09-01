package com.yuan.leopardkit.download.model;

import com.yuan.leopardkit.download.task.DownLoadSubscriber;
import com.yuan.leopardkit.download.task.DownLoadTask;
import com.yuan.leopardkit.models.FileLoadInfo;

/**
 * Created by Yuan on 2016/8/29.
 * Detail 下载信息实体类
 */
public class DownloadInfo extends FileLoadInfo {

    private DownLoadTask downLoadTask;

    private DownLoadSubscriber subscriber;


    public DownloadInfo() {
    }

    public DownLoadSubscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(DownLoadSubscriber subscriber) {
        this.subscriber = subscriber;
    }

    public DownLoadTask getDownLoadTask() {
        return downLoadTask;
    }

    public void setDownLoadTask(DownLoadTask downLoadTask) {
        this.downLoadTask = downLoadTask;
    }

}
