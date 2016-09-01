package cn.yuan.leopard.model;

import com.yuan.leopardkit.download.model.DownloadInfo;

/**
 * Created by Yuan on 2016/9/1.
 * Detail 下载适配器实体类
 */
public class DownLoadModel {

    private DownloadInfo info;

    private int picRes;
    private String appName;
    private int total;
    private int progress;

    public DownLoadModel() {
    }

    public DownLoadModel(int picRes, String appName, int total, int progress) {
        this.picRes = picRes;
        this.appName = appName;
        this.total = total;
        this.progress = progress;
    }

    public int getPicRes() {
        return picRes;
    }

    public void setPicRes(int picRes) {
        this.picRes = picRes;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public DownloadInfo getInfo() {
        return info;
    }

    public void setInfo(DownloadInfo info) {
        this.info = info;
    }
}
