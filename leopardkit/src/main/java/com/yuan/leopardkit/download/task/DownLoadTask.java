package com.yuan.leopardkit.download.task;

import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.http.LeopardClient;
import com.yuan.leopardkit.http.factory.DownLoadFileFactory;
import com.yuan.leopardkit.interfaces.FileRespondResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

/**
 * Created by Yuan on 2016/8/30.
 * Detail 单一职责 负责下载任务（基本下载操作、数据库更新、缓存处理）
 */
public class DownLoadTask {

    private final String TAG = "DownLoadTask";

    private int tagKey;//所有任务通过这个key来区别
    private int taskState;//当前任务状态
    private long startPoints = 0L;//记录当前断点位置
    private boolean isStart = true;//是否第一次开始

    private DownloadInfo downloadInfo;
    private FileRespondResult fileRespondResult;

    public DownLoadTask(DownloadInfo downloadInfo, FileRespondResult fileRespondResult) {
        this.downloadInfo = downloadInfo;
        taskState = this.downloadInfo.getState();
        this.fileRespondResult = fileRespondResult;
        download(true);
    }

    public void stop() {
        this.downloadInfo.getSubscriber().unsubscribe();
        if (this.downloadInfo.getState() != DownLoadManager.STATE_FINISH){
            DownLoadManager.getManager().removeTask(this.downloadInfo);
            File file = new File(downloadInfo.getFileSavePath() + downloadInfo.getFileName());
            if (file.exists()) file.delete();
        }
        // TODO: 2016/8/31 删除数据库记录
        HttpDbUtil.instance.delete(downloadInfo);
    }

    public void pause(long startPoints) {
        downloadInfo.setState(DownLoadManager.STATE_PAUSE);
        this.startPoints = startPoints;
        downloadInfo.setProgress(startPoints);
        this.downloadInfo.getSubscriber().unsubscribe();
        //// TODO: 2016/8/31 更新数据库
        HttpDbUtil.instance.updateState(downloadInfo);
    }

    public void resume(){
        download(false);
    }

    public void reStart(){
        download(true);
    }

    public void download( boolean isRestart) {
        isStart = isRestart;
        if (isRestart) {
            startPoints = 0L;
        }
        getClient().downLoadFile(this.downloadInfo, this.fileRespondResult, this);
    }


    private LeopardClient getClient() {
        return new LeopardClient.Builder()
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addDownLoadFileFactory(DownLoadFileFactory.create(this.fileRespondResult, this.downloadInfo, startPoints))
                .build();
    }

    public void writeCache(InputStream inputStream){
        String savePath = downloadInfo.getFileSavePath() + downloadInfo.getFileName();
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
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, downloadInfo.getProgress(), downloadInfo.getFileLength());
            byte[] buffer = new byte[1024];
            int len;
            int record = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                mappedBuffer.put(buffer, 0, len);
                record += len;
            }
        } catch (IOException e) {
//            Log.i(TAG, "yuan" + e.getMessage());
            e.printStackTrace();
        } finally {
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
        }
    }

    public void downFinsh() {
        // TODO: 2016/8/31 完成更新数据库
        downloadInfo.setState(DownLoadManager.STATE_FINISH);
        HttpDbUtil.instance.update(downloadInfo);

        fileRespondResult.onSuccess("");
    }

}
