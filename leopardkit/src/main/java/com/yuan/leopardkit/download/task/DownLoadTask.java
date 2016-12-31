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

    private String fileCacheNem = ".cache";
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
    }

    /**
     * 停止下载，为了再次重新下载
     */
    public void stop() {
        this.downloadInfo.setState(DownLoadManager.STATE_WAITING);
        if (this.downloadInfo!=null &&this.downloadInfo.getSubscriber() != null)
            this.downloadInfo.getSubscriber().unsubscribe();
        fileRespondResult.onExecuting(0L,downloadInfo.getFileLength(),false);
        if (this.downloadInfo.getState() != DownLoadManager.STATE_FINISH){
            resetProgress();
            File file = new File(downloadInfo.getFileSavePath() + downloadInfo.getFileName()+fileCacheNem);
            if (file.exists()) file.delete();
        }
//        if (this.downloadInfo.getState() == DownLoadManager.STATE_FINISH){
//            resetProgress();
//            File file = new File(downloadInfo.getFileSavePath() + downloadInfo.getFileName());
//            if (file.exists()) file.delete()
//        }
    }

    public void remove(){
        stop();
        HttpDbUtil.instance.delete(this.downloadInfo);
    }

    public void pause() {
        downloadInfo.setState(DownLoadManager.STATE_PAUSE);
        downloadInfo.setBreakProgress(downloadInfo.getProgress());//记录断点位置;
        if (this.downloadInfo!=null &&this.downloadInfo.getSubscriber() != null)
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

        if (downloadInfo.getState() == DownLoadManager.STATE_DOWNLOADING)return;

        if (downloadInfo.getState() == DownLoadManager.STATE_FINISH && !isRestart) return;
        isStart = isRestart;
        if (isRestart) {
            if (downloadInfo.getState() == DownLoadManager.STATE_FINISH )
                stop();
            resetProgress();
            startPoints = 0L;
        }
        downloadInfo.setState(DownLoadManager.STATE_DOWNLOADING);
        getClient().downLoadFile(this.downloadInfo, this.fileRespondResult, this);
    }


    private LeopardClient getClient() {
        return new LeopardClient.Builder()
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addDownLoadFileFactory(DownLoadFileFactory.create(this.fileRespondResult, this.downloadInfo))
                .build();
    }

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
        fileRespondResult.onSuccess("");
    }

    private void resetProgress(){
        this.downloadInfo.setBreakProgress(0L);
        this.downloadInfo.setProgress(0L);
        this.downloadInfo.setFileLength(0L);
    }

}
