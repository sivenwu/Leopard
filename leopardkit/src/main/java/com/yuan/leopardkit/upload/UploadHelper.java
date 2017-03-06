package com.yuan.leopardkit.upload;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.yuan.leopardkit.interfaces.UploadIProgress;
import com.yuan.leopardkit.servers.BaseServerApi;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Yuan on 2017/2/28.
 * Detail 上传辅助类
 */

public class UploadHelper {

    private final String TAG = "UploadHelper";

    private MediaType dataMediaType = MediaType.parse("multipart/form-data");

    private BaseServerApi api;
    private Subscription bodySubscriber;

    //control value
    private FileUploadEnetity fileUploadEnetity;
    private boolean isDone = false;
    private long curUploadProgress = 0;
    private long totalUploadProgress = 0;
    private int fileIndex = 1;//上传文件位置

    // callback
    private UploadIProgress iProgressLienter;

    // refresh the progress by timer
    private Timer refreshTimer;
    private TimerTask refreshTimerTask;

    //handler for mainThread
    private final int HANDLER_CODE = 0X66;
    private final int HANDLER_DELAY = 100;
    private Handler handler  = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (iProgressLienter != null && msg.what == HANDLER_CODE) {
                long curProgress = curUploadProgress;
                long totalProgress = totalUploadProgress;
                if (!isDone) {
                    isDone = curProgress >= totalProgress;
                    iProgressLienter.onProgress(curProgress, totalProgress, fileIndex, isDone);
                }
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

    public UploadHelper(BaseServerApi api) {
        this.api = api;
    }

    public void upload(final FileUploadEnetity enetity, final UploadIProgress callback){
        try {

            isDone = false;
            this.fileUploadEnetity = enetity;

            URL url = url = new URL(enetity.getUrl());
            Observable
                    .just(url)
                    .map(new Func1<URL, Integer>() {
                        @Override
                        public Integer call(URL url) {
                            return fiterUpload(url,callback);
                        }
                    })
                    .compose(cfgTransformer)
                    .onBackpressureBuffer()
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer integer) {
                            if (integer != 404){
                                justUpLoad(enetity,callback);
                            }else{
                                callback.onFailed(null,"HTTP 404 not found!");
                            }
                        }
                    });
        } catch (MalformedURLException e) {
            callback.onFailed(e,e.getMessage().toString());
            e.printStackTrace();
        }
    }

    private void justUpLoad(final FileUploadEnetity enetity, final UploadIProgress callback){
        try {
            this.iProgressLienter = callback;
            URL url = new URL(enetity.getUrl());
            resetValue();

            HashMap<String, RequestBody> params  = displayFile(enetity);
            if (params!=null)
                uploading(url.getPath(),params);
        } catch (MalformedURLException e) {
            this.iProgressLienter.onFailed(e,e.getMessage().toString());
            e.printStackTrace();
        }
    }

    private void uploading(String url, HashMap<String, RequestBody> params){
        startRefreshTimer();
        bodySubscriber =  api.uploadFile(url,params)
                .compose(cfgTransformer)
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() { // do no any things
                        closeUpload();//完成关闭
                    }

                    @Override
                    public void onError(Throwable e) { // do no any things
                        iProgressLienter.onFailed(e,e.getMessage().toString());
                        closeUpload();//失败关闭
                        Log.e(TAG,"[Subscriber] "+"onError: "+e.getMessage().toString());
                    }

                    @Override
                    public void onNext(ResponseBody o) { // do no any things
                        try {
                            String result = new String(o.bytes(),"utf-8");
                            if (!isDone) {
                                iProgressLienter.onProgress(fileUploadEnetity.getFilesTotalSize(), fileUploadEnetity.getFilesTotalSize(),
                                        fileUploadEnetity.getFileNum(), true);
                                isDone = true;
                            }
                            iProgressLienter.onSucess(result);
                            Log.d(TAG,"[Subscriber] "+"onNext: "+result);
                        } catch (IOException e) {
                            e.printStackTrace();
                            iProgressLienter.onFailed(e,e.getMessage().toString());
                        }
                    }
                });
    }

    public void closeUpload(){
        resetValue();
        stopRefreshTimer();
        if (bodySubscriber != null && !bodySubscriber.isUnsubscribed()){
            bodySubscriber.unsubscribe();
        }
    }

    private void startRefreshTimer(){
        refreshTimer = new Timer();
        refreshTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (isDone){
                    stopRefreshTimer();
                    return;
                }
                handler.sendEmptyMessage(HANDLER_CODE);
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
     * method for close
     */
    private void resetValue(){
        this.curUploadProgress = 0;
        this.fileIndex = 1;
    }

    /**
     * to config the parameters of request
     * @param enetity
     * @return
     */
    private HashMap<String, RequestBody> displayFile(final FileUploadEnetity enetity){
        HashMap<String, RequestBody> params = null;
        final List<File> files = enetity.getFiles();

        if (files.size() <= 0){
            Log.d(TAG,"[displayFile] "+"upload no found file!");
            return null;
        }

        params = new HashMap<>();
        for (int i = 0; i < files.size(); i++) {
            final File file = files.get(i);
            RequestBody body =
                    RequestBody.create(dataMediaType, file);

            UploadFileRequestBody body_up = new UploadFileRequestBody(body, new UploadFileRequestBody.UploadBodyListener() {
                @Override
                public void onProgress(long progress, long total, boolean done) {//每次done表示一个文件完成
                    if (done) {
                        if (fileIndex > files.size())
                            fileIndex++;
                    }
                    curUploadProgress = progress;
                    totalUploadProgress = enetity.getFilesTotalSize();
//                    handler.sendMessageDelayed(getMessage(curUploadProgress + (progress),enetity.getFilesTotalSize()),HANDLER_DELAY);
                }
            });
            params.put("file[]\"; filename=\"" + file.getName(), body_up);
        }
        return params;
    }

    // 过滤状态码
    private int fiterUpload(URL url, UploadIProgress callback){
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            return conn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailed(e,e.getMessage().toString());
            return 404;
        }
    }

    private Message getMessage(long progress, long total){
        Message message = new Message();
        message.what = HANDLER_CODE;
        message.arg1 = (int) progress;
        message.arg2 = (int) total;
        return message;
    }

}
