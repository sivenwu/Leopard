package com.yuan.leopardkit.download.model;

import com.yuan.leopardkit.download.model.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuan on 2016/8/26.
 * Detail 文件下载包装类 暂时屏蔽多下载
 */
public class FileDwonEnetity {

    private List<DownloadInfo> downloadInfos = new ArrayList<>();

    public FileDwonEnetity(DownloadInfo info) {
        downloadInfos.add(0, info);
    }


    public List<DownloadInfo> getDownloadInfos() {
        return downloadInfos;
    }

    public DownloadInfo getDownloadInfo() {
        if (downloadInfos.size() > 0) {
            return downloadInfos.get(0);
        }
        return null;
    }

}
