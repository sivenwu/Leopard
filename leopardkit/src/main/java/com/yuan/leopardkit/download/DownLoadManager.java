package com.yuan.leopardkit.download;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.ArrayMap;
import android.util.SparseArray;

import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.download.task.DownLoadTask;
import com.yuan.leopardkit.interfaces.FileRespondResult;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
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
    private SparseArray taskArray;

    private int limitNum = -1;//默认不限制现在数量

    public static DownLoadManager getManager(){
        if (manager == null){
            manager = new DownLoadManager();
        }
        return manager;
    }

    private DownLoadManager() {
        //初始化一系列
        deFaultDir = Environment.getExternalStorageDirectory() + "/YuanDwonload/";
        downloadInfosList = new ArrayList<>();
        taskArray = new SparseArray();
        if (!new File(deFaultDir).exists()) new File(deFaultDir).mkdirs();
    }

    public void setLimitTaskNum(int num){
        this.limitNum = num;
    }

    private boolean limitVaild(){
        if (this.limitNum == -1) return true;
        else{
            return downloadInfosList.size() > limitNum;
        }
    }

    public void writeCache(DownloadInfo downloadInfo, InputStream is){
        DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
        task.writeCache(is);
    }

    public boolean addTask(DownloadInfo downloadInfo, FileRespondResult listener){

        // 如果设置了限制 非有效则拒绝添加任务
        if (!limitVaild()){ return false;}

        if (downloadInfo.getFileSavePath() == null || downloadInfo.getFileSavePath().equals("")){
            downloadInfo.setFileSavePath(deFaultDir);
        }
        DownLoadTask task = new DownLoadTask(downloadInfo,listener);
//        downloadInfo.setDownLoadTask(task);
        downloadInfosList.add(downloadInfo);
        // TODO: 2016/8/31 添加一条记录
        long key =  HttpDbUtil.instance.insert(downloadInfo);
        taskArray.put((int) key,task);
        downloadInfo.setKey(key);
        return true;
    }

    public void removeTask(DownloadInfo downloadInfo){
        if (downloadInfosList.contains(downloadInfo)) {
            downloadInfosList.remove(downloadInfo);
            File downFile = new File(downloadInfo.getFileSavePath() + downloadInfo.getFileName());
            if (downFile.exists()){
                downFile.delete();
            }
            taskArray.remove((int) downloadInfo.getKey());
        }
        HttpDbUtil.instance.delete(downloadInfo);
    }

    public void removeAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
            task.remove();
        }
        downloadInfosList.clear();
        taskArray.clear();
    }

    public void pauseAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            pauseTask(downloadInfo);
        }
    }

    public void restartAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            restartTask(downloadInfo);
        }
    }

    public void startAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            startTask(downloadInfo);
        }
    }

    public void stopAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            stopTask(downloadInfo);
        }
    }

    public void resumeTask(DownloadInfo downloadInfo){
        DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
        task.download(false);
    }

    public void resumeAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            resumeTask(downloadInfo);
        }
    }

    public void restartTask(DownloadInfo downloadInfo){
        DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
        task.download(true);
    }

    public void startTask(DownloadInfo downloadInfo){
        DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
        task.download(false);
    }

    public void stopTask(DownloadInfo downloadInfo){
        DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
        task.stop();
    }

    public void pauseTask(DownloadInfo downloadInfo) {
        DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
        task.pause();
    }

}
