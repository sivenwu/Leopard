package com.yuan.leopardkit.download.utils;

import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;

/**
 * Created by Yuan on 2017/3/2.
 * Detail 状态判断工具类
 */

public class DownloadStateUtil {

    public static boolean isFinsh(DownloadInfo downloadInfo){
        return downloadInfo.getState() == DownLoadManager.STATE_FINISH;
    }

    public static boolean isDownloading(DownloadInfo downloadInfo){
        return downloadInfo.getState() == DownLoadManager.STATE_DOWNLOADING;
    }

    public static boolean isError(DownloadInfo downloadInfo){
        return downloadInfo.getState() == DownLoadManager.STATE_ERROR;
    }

    public static boolean isPause(DownloadInfo downloadInfo){
        return downloadInfo.getState() == DownLoadManager.STATE_PAUSE;
    }

    public static boolean isWaiting(DownloadInfo downloadInfo){
        return downloadInfo.getState() == DownLoadManager.STATE_WAITING;
    }

}
