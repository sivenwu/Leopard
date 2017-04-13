package com.yuan.leopardkit.download;

import android.content.Context;
import android.os.Environment;
import android.util.SparseArray;

import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.download.task.DownLoadTask;
import com.yuan.leopardkit.download.utils.DownloadStateUtil;
import com.yuan.leopardkit.interfaces.FileRespondResult;
import com.yuan.leopardkit.interfaces.IDownloadProgress;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



/**
 * Created by Yuan on 2016/8/30.
 * Detail 下载辅助类,管理下载任务 与UI进行回馈
 */
public class DownLoadManager {

    private final String TAG = "DownLoadManager";

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

    public long addTask(DownloadInfo downloadInfo, IDownloadProgress listener){

        // 如果添加过，就直接没必要再次添加了
        if (downloadInfosList.contains(downloadInfo)) return 1;

        // 如果设置了限制 非有效则拒绝添加任务
        if (!limitVaild()){ return -1;}

        if (downloadInfo.getFileSavePath() == null || downloadInfo.getFileSavePath().equals("")){
            downloadInfo.setFileSavePath(deFaultDir);
        }
//        downloadInfo.setDownLoadTask(task);
        downloadInfosList.add(downloadInfo);
        // TODO: 2016/8/31 添加一条记录
        long key =  HttpDbUtil.instance.insert(downloadInfo);
        downloadInfo.setKey(key);

        DownLoadTask task = new DownLoadTask(downloadInfo,listener);
        taskArray.put((int) key,task);
        return key;
    }

    private void checkAddTask(){

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
//        Log.i(TAG, "startAllTask now n the number of downloadList is " + downloadInfosList.size() + "");
        for (DownloadInfo downloadInfo : downloadInfosList){
            // 暂停状态是恢复下载
            if (DownloadStateUtil.isPause(downloadInfo)){
//                Log.i(TAG, "startAllTask: " +"startTask , the id is "+downloadInfo.getKey());
                startTask(downloadInfo);
            }else{
                restartTask(downloadInfo);
//                Log.i(TAG, "startAllTask: " +"restartTask, the id is "+downloadInfo.getKey());
            }
        }
    }

    public void stopAllTask(){
        for (DownloadInfo downloadInfo : downloadInfosList){
            stopTask(downloadInfo);
        }
    }

    public void resumeTask(DownloadInfo downloadInfo){
        if (!DownloadStateUtil.isFinsh(downloadInfo)) {
            DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
            task.download(false);
        }
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
        if (!DownloadStateUtil.isFinsh(downloadInfo)) {
            DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
            task.download(false);
        }
    }

    public void stopTask(DownloadInfo downloadInfo){
        DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
        task.stop();
    }

    public void pauseTask(DownloadInfo downloadInfo) {
        if (!DownloadStateUtil.isFinsh(downloadInfo)) {
            DownLoadTask task = (DownLoadTask) taskArray.get((int) downloadInfo.getKey());
            task.pause();
        }
    }

    /**
     * 由于会持有定时器资源，防止内存泄露的几点建议
     * 1、下载进程最好存在后台，回调在前台
     * 2、下载引用准备销毁时，建议调用 DownLoadManager.getManager().release() 方法，释放定时器资源
     * 3、后期优化..
     */
    public void release(){
        pauseAllTask();
    }

    /**
     * 获取下载列表
     */

    public List<DownloadInfo> getDownloadList(Context context){

        if (HttpDbUtil.instance == null)
            HttpDbUtil.initHttpDB(context.getApplicationContext());

        return  HttpDbUtil.instance.queryFileInfo(0);
    }

}
