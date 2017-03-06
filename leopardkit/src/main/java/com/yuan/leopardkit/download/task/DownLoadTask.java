package com.yuan.leopardkit.download.task;

import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.interfaces.IDownloadProgress;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Yuan on 2016/8/30.
 * Detail 单一职责 负责下载任务（基本下载操作、数据库更新、缓存处理）
 *        1.4 版本之后 职责只负责状态变化，缓存处理。下载职责给downhelper
 */
public class DownLoadTask {

    private final String TAG = "DownLoadTask";

    private String fileCacheNem = ".cache";
    private int tagKey;//所有任务通过这个key来区别
    private int taskState;//当前任务状态
    private long startPoints = 0L;//记录当前断点位置
    private boolean isStart = true;//是否第一次开始

    private DownloadInfo downloadInfo;
//    private FileRespondResult fileRespondResult;

    // 1.4 downloadhelper
    private DownLoadHelper downLoadHelper;
    private IDownloadProgress iDownloadProgress;

    public DownLoadTask(DownloadInfo downloadInfo, IDownloadProgress iDownloadProgress) {
        this.downloadInfo = downloadInfo;
        this.iDownloadProgress = iDownloadProgress;
        this.downLoadHelper = new DownLoadHelper(this.downloadInfo,this.iDownloadProgress);

        taskState = this.downloadInfo.getState();
    }

    /**
     * 停止下载，为了再次重新下载
     */
    public void stop() {
        this.downloadInfo.setState(DownLoadManager.STATE_WAITING);

        // 委托停止
        downLoadHelper.stop();
//
//        //
//        if (!isFinish()){
//            resetProgress();
//            File file = new File(downloadInfo.getFileSavePath() + downloadInfo.getFileName()+fileCacheNem);
//            if (file.exists()) file.delete();
//        }
    }

    public void remove(){
        stop();

        File file = new File(downloadInfo.getFileSavePath() + downloadInfo.getFileName()+fileCacheNem);
        if (file.exists()) file.delete();

        HttpDbUtil.instance.delete(this.downloadInfo);
    }

    public void pause() {
        downloadInfo.setState(DownLoadManager.STATE_PAUSE);
        downloadInfo.setBreakProgress(downloadInfo.getProgress());//记录断点位置;

        // 委托暂停
        downLoadHelper.pause();

        // TODO: 2016/8/31 更新数据库
        HttpDbUtil.instance.updateState(downloadInfo);
    }

    public void resume(){
        download(false);
    }

    public void reStart(){
        download(true);
    }

    public void download( boolean isRestart) {

        if (downloadInfo.getState() == DownLoadManager.STATE_DOWNLOADING)return;

        if (downloadInfo.getState() == DownLoadManager.STATE_FINISH && !isRestart) return;

        // 更新start状态
        isStart = isRestart;
        downLoadHelper.setStart(isRestart);

        if (isRestart) {
            if (downloadInfo.getState() == DownLoadManager.STATE_FINISH )
                stop();
            resetProgress();
            startPoints = 0L;
        }
        downloadInfo.setState(DownLoadManager.STATE_DOWNLOADING);

        // 开启委托下载
        downLoadHelper.start(downloadInfo,iDownloadProgress);
    }

    // ---------------------------------------------------------------------------------------------
    public void writeCache(InputStream inputStream){

        File fileDir = new File(downloadInfo.getFileSavePath());
        if (!fileDir.exists()){
            fileDir.mkdirs();
        }

        String savePath = downloadInfo.getFileSavePath() + downloadInfo.getFileName() +fileCacheNem;
        File file = new File(savePath);
        if (isStart){//如果是重新下载
            if (file.exists()){
                file.delete();
            }
            isStart = false;
        }
        FileChannel channelOut = null;
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rwd");
            channelOut = randomAccessFile.getChannel();
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, downloadInfo.getBreakProgress(), downloadInfo.getFileLength()-downloadInfo.getBreakProgress());
            byte[] buffer = new byte[1024];
            int len;
            int record = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                mappedBuffer.put(buffer, 0, len);
                record += len;
            }

            try {
                inputStream.close();
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
//            Log.i(TAG, "yuan" + e.getMessage());
            e.printStackTrace();
        } finally {

        }
    }

    public void downFinsh() {
        // TODO: 2016/8/31 完成更新数据库
        downloadInfo.setState(DownLoadManager.STATE_FINISH);
        HttpDbUtil.instance.update(downloadInfo);

        //更新文件
        File file = new File(downloadInfo.getFileSavePath() + downloadInfo.getFileName() +fileCacheNem);
        file.renameTo(new File(downloadInfo.getFileSavePath() + downloadInfo.getFileName()));
    }

    private void resetProgress(){
        this.downloadInfo.setBreakProgress(0L);
        this.downloadInfo.setProgress(0L);
        this.downloadInfo.setFileLength(0L);
    }

    // ---- 状态判断入口
    private boolean isFinish(){
        return this.downloadInfo.getState() == DownLoadManager.STATE_FINISH;
    }

}
