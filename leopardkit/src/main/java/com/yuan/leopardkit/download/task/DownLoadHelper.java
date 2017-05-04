package com.yuan.leopardkit.download.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownLoadResponseBody;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.download.utils.DownloadStateUtil;
import com.yuan.leopardkit.http.factory.DownLoadFileFactory;
import com.yuan.leopardkit.interfaces.IDownloadProgress;
import com.yuan.leopardkit.servers.BaseServerApi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yuan on 2017/3/1.
 * Detail 下载辅助类,职责只负责下载任务，以及主线程回调处理
 */

public class DownLoadHelper {

    private final String TAG = "DownLoadHelper";
    private final String fileCacheNem = ".cache";

    // info
    private DownloadInfo downloadInfo;
    private boolean isStart = true;// 是否是重新下载

    // http
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    private BaseServerApi api;
    private Subscription bodySubscriber;

    // callback
    private IDownloadProgress iDownloadProgress;

    private String failedMessage;
    private long curUploadProgress = 0;
    private long totalUploadProgress = 0;

    // refresh the progress by timer
    private Timer refreshTimer;
    private TimerTask refreshTimerTask;

    //handler for mainThread
    private final int HANDLER_CODE = 0X66;
    private final int HANDLER_CODE_FAILED = 0X88;
    private Handler handler  = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (iDownloadProgress!=null && msg.what == HANDLER_CODE){
                if (totalUploadProgress != 0) {
                    // 如果完成了下载，关闭download
                    if (curUploadProgress >= totalUploadProgress || DownloadStateUtil.isPause(downloadInfo)){ closeDownload();}
                    iDownloadProgress.onProgress(downloadInfo.getKey(),curUploadProgress, totalUploadProgress, curUploadProgress >= totalUploadProgress);
                }

                HttpDbUtil.instance.updateState(downloadInfo);
            }else if (iDownloadProgress!=null && msg.what == HANDLER_CODE_FAILED){
                iDownloadProgress.onFailed(null,failedMessage);
            }
        }
    };

    private final Observable.Transformer cfgTransformer = new Observable.Transformer() {
        @Override
        public Object call(Object observable) {
            return ((Observable) observable)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    };

    public DownLoadHelper(DownloadInfo downloadInfo, IDownloadProgress iDownloadProgress) {
        this.downloadInfo = downloadInfo;
        this.iDownloadProgress = iDownloadProgress;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    private void download(DownloadInfo downloadInfo, IDownloadProgress iDownloadProgress){
        try {
            this.downloadInfo = downloadInfo;
            this.iDownloadProgress = iDownloadProgress;

            this.downloadInfo.setState(DownLoadManager.STATE_DOWNLOADING);

            URL _url =  new URL(this.downloadInfo.getUrl());
            if (_url.getPort() != -1) {
                initClient(_url.getProtocol() + "://" + _url.getHost() + ":" + _url.getPort() + "/");
            }else{
                initClient(_url.getProtocol() + "://" + _url.getHost()+ "/");
            }
            justDownload();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void justDownload(){
        String url = this.downloadInfo.getUrl();

        bodySubscriber = Observable.create(new Observable.OnSubscribe<ResponseBody>() {
            @Override
            public void call(final Subscriber<? super ResponseBody> subscriber) {
                if (downloadInfo.getState() == DownLoadManager.STATE_PAUSE){//如果暂停就不请求了
                    return ;
                }
                Request request = new Request.Builder().url(downloadInfo.getUrl()).build();

                okHttpClient.newCall(request)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                downloadInfo.setState(DownLoadManager.STATE_ERROR);
                                // TODO: 2016/8/31 更新数据库
                                HttpDbUtil.instance.updateState(downloadInfo);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (downloadInfo.getFileLength() <= 0)
                                    downloadInfo.setFileLength(response.body().contentLength());
                                // TODO: 2017/5/4 URL有效，现在文件为无效的时候直接停止回调 
                                if (downloadInfo.getFileLength() <= 0){
                                    failedMessage = "This file is invalid";
                                    handler.sendEmptyMessage(HANDLER_CODE_FAILED);
                                    stop();
                                }else {
                                    DownLoadManager.getManager().writeCache(downloadInfo, response.body().byteStream());
                                    // TODO: 2016/8/31 更新数据库 这里记得做下数据库延时更新
                                    HttpDbUtil.instance.updateState(downloadInfo);
                                }

                                if (downloadInfo.getState() != DownLoadManager.STATE_PAUSE)
                                    subscriber.onNext(response.body());
                            }
                        });
            }
        })
                //避免doing too much work on its main thread.
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                        closeDownload();
                        Log.i(TAG, "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage() != null)
                        iDownloadProgress.onFailed(e,e.getMessage().toString());
                        closeDownload();//失败关闭
                    }

                    @Override
                    public void onNext(ResponseBody o) {
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
                                downFinsh();
//                            }
//                        }).start();
                    }
                });
    }

    private void initClient(String url){
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(DownLoadFileFactory.create(new DownLoadResponseBody.DownLoadBodyListener() {
                    @Override
                    public void onProgress(long progress, long total, boolean done) {
                        curUploadProgress = progress;
                        totalUploadProgress = total;
                    }

                    @Override
                    public void onFailed(String message) {
                        failedMessage = message;
                        handler.sendEmptyMessage(HANDLER_CODE_FAILED);
                    }
                }, this.downloadInfo))
                .build();

        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(url)
                .build();

        api = retrofit.create(BaseServerApi.class);
    }

    /**
     * 下载入口，从0开始
     */
    public void start(DownloadInfo downloadInfo, IDownloadProgress iDownloadProgress){
        startRefreshTimer();
        download(downloadInfo,iDownloadProgress);
    }

    private void start(){
        start(this.downloadInfo,this.iDownloadProgress);
    }

    public void stop(){
        if (this.bodySubscriber != null){
            this.bodySubscriber.unsubscribe();
            this.bodySubscriber = null;
        }
        stopRefreshTimer();
//        if (DownloadStateUtil.isWaiting(downloadInfo)) {
//            this.iDownloadProgress.onProgress(downloadInfo.getKey(), 0L, downloadInfo.getFileLength(), false); // 回调说明停止了下载
//        }else if (DownloadStateUtil.isPause(downloadInfo)){
//            this.iDownloadProgress.onProgress(downloadInfo.getKey(), downloadInfo.getBreakProgress(), downloadInfo.getFileLength(), false); // 回调说明停止了下载
//        }
    }

    public void pause(){
        stop();
    }

    public void closeDownload(){
        stop();
    }

    public void resume(){
        start();
    }

    private void startRefreshTimer(){

        stopRefreshTimer();

        refreshTimer = new Timer();
        refreshTimerTask = new TimerTask() {
            @Override
            public void run() {
                    handler.sendEmptyMessage(HANDLER_CODE);
//                    Log.i(TAG, "run: ------- timer ----- " + downloadInfo.getKey());
            }
        };

        refreshTimer.schedule(refreshTimerTask,1000,1000);
    }

    private void stopRefreshTimer(){

        if (refreshTimer != null){
            refreshTimer.cancel();
            refreshTimer = null;
        }

        if (refreshTimerTask != null){
            refreshTimerTask.cancel();
            refreshTimerTask = null;
        }
    }

    /**
     * for download -- save file
     * @param downloadInfo
     * @param inputStream
     */
    public void writeFile(DownloadInfo downloadInfo, InputStream inputStream) {
        String savePath = downloadInfo.getFileSavePath() + downloadInfo.getFileName();
        File file = new File(savePath);
        File dir = new File(downloadInfo.getFileSavePath());
        if (!dir.exists()){
            dir.mkdirs();
        }
        if (file.exists()){
            file.delete();
        }
        FileChannel channelOut = null;
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rwd");
            channelOut = randomAccessFile.getChannel();
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, 0, downloadInfo.getFileLength());
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
                Log.e("DownLoadHelper","[writeCache]"+" IOException :"+e.getMessage().toString());
                e.printStackTrace();
            }
        } catch (IOException e) {
            Log.e("DownLoadHelper","[writeCache]"+" IOException :"+e.getMessage().toString());
            e.printStackTrace();
        }
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
    }


}
