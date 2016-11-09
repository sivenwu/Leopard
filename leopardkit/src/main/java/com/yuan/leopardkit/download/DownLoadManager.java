package com.yuan.leopardkit.download;

import android.os.Environment;

import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.download.task.DownLoadTask;
import com.yuan.leopardkit.interfaces.FileRespondResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



/**
 * Created by Yuan on 2016/8/30.
 * Detail 下载辅助类,管理下载任务 与UI进行回馈
 */
public class DownLoadManager {

    private static DownLoadManager manager = new DownLoadManager();

    //定义下载状态常量
    public static final int STATE_WAITING = 1;      //等待    --> 下载，暂停
    public static final int STATE_DOWNLOADING = 2;  //下载中  --> 暂停，完成，错误
    public static final int STATE_PAUSE = 3;        //暂停    --> 等待，下载
    public static final int STATE_FINISH = 4;       //完成    --> 重新下载
    public static final int STATE_ERROR = 5;        //错误    --> 等待

    public String deFaultDir = Environment.getExternalStorageDirectory() + "/YuanDwonload/";

    private FileRespondResult callback;
    private List<DownloadInfo> downloadInfosList;

    public static DownLoadManager getManager(){
        if (manager == null){
            manager = new DownLoadManager();
        }
        return manager;
    }

    public DownLoadManager() {
        //初始化一系列
        deFaultDir = Environment.getExternalStorageDirectory() + "/YuanDwonload/";
        downloadInfosList = new ArrayList<>();
        if (!new File(deFaultDir).exists()) new File(deFaultDir).mkdirs();
    }

    public void addTask(DownloadInfo downloadInfo, FileRespondResult listener){
        if (downloadInfo.getFileSavePath() == null || downloadInfo.getFileSavePath().equals("")){
            downloadInfo.setFileSavePath(deFaultDir);
        }
        DownLoadTask task = new DownLoadTask(downloadInfo,listener);
        downloadInfo.setDownLoadTask(task);
        downloadInfosList.add(downloadInfo);
        // TODO: 2016/8/31 添加一条记录
        long key =  HttpDbUtil.instance.insert(downloadInfo);
        downloadInfo.setKey(key);
    }

    public void removeTask(DownloadInfo downloadInfo){
        if (downloadInfosList.contains(downloadInfo)) {
            downloadInfosList.remove(downloadInfo);
            File downFile = new File(downloadInfo.getFileSavePath() + downloadInfo.getFileName());
            if (downFile.exists()){
                downFile.delete();
            }
        }
        HttpDbUtil.instance.delete(downloadInfo);
    }

    public void removeAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            downloadInfo.getDownLoadTask().remove();
        }
        downloadInfosList.clear();
    }

    public void pauseAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            downloadInfo.getDownLoadTask().pause();
        }
    }

    public void restartAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            downloadInfo.getDownLoadTask().download(true);
        }
    }

    public void startAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            downloadInfo.getDownLoadTask().download(false);
        }
    }

    public void stopAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            downloadInfo.getDownLoadTask().stop();
        }
    }

    public void restartTask(DownloadInfo downloadInfo){
        downloadInfo.getDownLoadTask().download(true);
    }

    public void startTask(DownloadInfo downloadInfo){
        downloadInfo.getDownLoadTask().download(false);
    }

    public void stopTask(DownloadInfo downloadInfo){
        downloadInfo.getDownLoadTask().stop();
    }

    public void pauseTask(DownloadInfo downloadInfo) {
      downloadInfo.getDownLoadTask().pause();
    }

}
