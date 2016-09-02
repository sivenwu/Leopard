package com.yuan.leopardkit.http;

import android.content.Context;
import android.util.Log;

import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.download.task.DownLoadSubscriber;
import com.yuan.leopardkit.download.task.DownLoadTask;
import com.yuan.leopardkit.http.base.BaseEnetity;
import com.yuan.leopardkit.http.base.BaseSubscriber;
import com.yuan.leopardkit.http.factory.DownLoadFileFactory;
import com.yuan.leopardkit.http.factory.HeaderAddFactory;
import com.yuan.leopardkit.http.factory.RequestJsonFactory;
import com.yuan.leopardkit.http.factory.UploadFileFactory;
import com.yuan.leopardkit.interfaces.FileRespondResult;
import com.yuan.leopardkit.interfaces.HttpRespondResult;
import com.yuan.leopardkit.interfaces.IProgress;
import com.yuan.leopardkit.servers.BaseServerApi;
import com.yuan.leopardkit.upload.FileUploadEnetity;
import com.yuan.leopardkit.upload.UploadFileRequestBody;
import com.yuan.leopardkit.utils.JsonParseUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yuan on 2016/8/23.
 * Detail  Retrofit http管理类 build模式构建
 */
public class LeopardClient {

    private String TAG = "LeopardClient";

    private Context mContext;

    private boolean isJson = false;
    private String baseUrl = "";
    private static final int TIME_OUT = 30 * 1000;
    private static final String CONTENT_TYPE = "application/json";

    private BaseServerApi serverApi;
    private Retrofit retrofit;
    private Retrofit.Builder retrofitBuilder;
    private OkHttpClient okHttpClient;
    private OkHttpClient.Builder okHttpClientBuilder;

    private MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");
    private MediaType dataMediaType = MediaType.parse("multipart/form-data");

    public LeopardClient(BaseServerApi serverApi, Retrofit retrofit, Retrofit.Builder retrofitBuilder, OkHttpClient okHttpClient, OkHttpClient.Builder okHttpClientBuilder, boolean isJson) {
        this.serverApi = serverApi;
        this.retrofit = retrofit;
        this.retrofitBuilder = retrofitBuilder;
        this.okHttpClient = okHttpClient;
        this.okHttpClientBuilder = okHttpClientBuilder;
        this.isJson = isJson;
    }

    public void POST(Context context, BaseEnetity entity, HttpRespondResult callback) {
        mContext = context;
        if (isJson) {
            String json = JsonParseUtil.modeToJson(entity);
            Log.i("yuan", fiterURLFromJSON(json));
            RequestBody body = RequestBody.create(jsonMediaType, fiterURLFromJSON(json));
            serverApi
                    .postJSON(entity.getRuqestURL(), body)
                    .compose(schedulersTransformer)
                    .subscribe(new BaseSubscriber(mContext, callback));
        } else {
            serverApi
                    .post(entity.getRuqestURL(), fiterURLFromRequestParams(entity.getMapEnticty()))
                    .compose(schedulersTransformer)
                    .subscribe(new BaseSubscriber(mContext, callback));
            ;
        }
    }

    public void GET(Context context, BaseEnetity entity, HttpRespondResult callback) {
        mContext = context;
        if (isJson) {
            String json = JsonParseUtil.modeToJson(entity);
            RequestBody body = RequestBody.create(jsonMediaType, fiterURLFromJSON(json));
            serverApi
                    .getJSON(entity.getRuqestURL(), body)
                    .compose(schedulersTransformer)
                    .subscribe(new BaseSubscriber(mContext, callback));
        } else {
            serverApi
                    .get(entity.getRuqestURL(), fiterURLFromRequestParams(entity.getMapEnticty()))
                    .compose(schedulersTransformer)
                    .subscribe(new BaseSubscriber(mContext, callback));
            ;
        }
    }

//    public void upLoadFile(Context context, FileEnetity enetity, final FileRespondResult callback) {
//        mContext = context;
//        RequestBody body =
//                RequestBody.create(dataMediaType, enetity.getSimpleFile());
//        UploadFileRequestBody body_up = new UploadFileRequestBody(body, new ProgressListener() {
//            @Override
//            public void onProgress(long progress, long total, boolean done) {
//                callback.onExecuting(progress, total, done);
//            }
//        });
//        serverApi
//                .upLoadFile(enetity.getUrl(), body_up)
//                .compose(schedulersTransformer)
//                .subscribe(new BaseSubscriber(mContext, callback));
//    }

    long curUploadProgress = 0;

    public void upLoadFiles(Context context, final FileUploadEnetity enetity, final FileRespondResult callback) {
        mContext = context;
        curUploadProgress = 0;
        List<File> files = enetity.getFiles();
        HashMap<String, RequestBody> params = new HashMap<>();
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            RequestBody body =
                    RequestBody.create(dataMediaType, file);

            UploadFileRequestBody body_up = new UploadFileRequestBody(body, new IProgress() {
                @Override
                public void onProgress(long progress, long total, boolean done) {
                    if (done) {
                        curUploadProgress += total;
                    }
                    callback.onExecuting(curUploadProgress + (progress), enetity.getFilesTotalSize(), curUploadProgress + (progress) == enetity.getFilesTotalSize());
                }
            });
            params.put("file[]\"; filename=\"" + file.getName(), body_up);
        }

        serverApi.uploadFile(enetity.getUrl(), params)
                .compose(schedulersTransformer)
                .subscribe(new BaseSubscriber(mContext, callback));
    }

    public void downLoadFile(final DownloadInfo downloadInfo, FileRespondResult callback, DownLoadTask task){
        // call 缓存实现
//        serverApi.downloadFile(downloadInfo.getDownloadUrl())
//                .compose(schedulersTransformer)
//                .subscribe(new DownLoadSubscriber(callback,downloadInfo,task));
        Observable.create(new Observable.OnSubscribe<ResponseBody>() {
            @Override
            public void call(Subscriber<? super ResponseBody> subscriber) {
                if (downloadInfo.getState() == DownLoadManager.STATE_PAUSE){//如果暂停就不请求了
                    return ;
                }
                Request request = new Request.Builder().url(downloadInfo.getUrl()).build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (downloadInfo.getFileLength() <= 0)
                    downloadInfo.setFileLength(response.body().contentLength());
                    downloadInfo.getDownLoadTask().writeCache(response.body().byteStream());
                    // TODO: 2016/8/31 更新数据库 这里记得做下数据库延时更新
                    HttpDbUtil.instance.updateState(downloadInfo);
                    subscriber.onNext(response.body());
                } catch (IOException e) {
                    downloadInfo.setState(DownLoadManager.STATE_ERROR);
                    // TODO: 2016/8/31 更新数据库
                    HttpDbUtil.instance.updateState(downloadInfo);
                    e.printStackTrace();
                }
            }
        })
                //避免doing too much work on its main thread.
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DownLoadSubscriber(callback,downloadInfo,task));
    }

//    public DownLoadManager downLoadFile( FileDwonEnetity enetity, final FileRespondResult callback) {
//        for (Interceptor interceptor : okHttpClient.interceptors()) {
//            if (interceptor instanceof DownLoadFileFactory) {
//                DownLoadFileFactory factory = (DownLoadFileFactory) interceptor;
//                factory.setDownloadInfo(enetity.getDownloadInfo());
//                factory.setProgressListener(new ProgressListener() {
//                    @Override
//                    public void onProgress(long progress, long total, boolean done) {
//                        callback.onExecuting(progress, total, done);
//                    }
//                });
//            }
//        }
//
////        DownLoadManager downLoadManager =  new DownLoadManager(callback,enetity.getDownloadInfos(),serverApi);
//        DownLoadManager downLoadManager = new DownLoadManager(callback,enetity.getDownloadInfos(),serverApi);
//        downLoadManager.downloadStart();
//        return downLoadManager;
//    }

    final Observable.Transformer schedulersTransformer = new Observable.Transformer() {
        @Override
        public Object call(Object observable) {
            return ((Observable) observable)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    ;
        }
    };

    public static class Builder {

        private String baseUrl = "http://127.0.0.1";
        private int TIME_OUT = 30 * 1000;
        private boolean isLog = true;
        private boolean isJson = false;

        private BaseServerApi serverApi;
        private Retrofit retrofit;
        private Retrofit.Builder retrofitBuilder;
        private OkHttpClient okHttpClient;
        private OkHttpClient.Builder okHttpClientBuilder;

        //Factory
        private GsonConverterFactory gsonConverterFactory;
        private RxJavaCallAdapterFactory javaCallAdapterFactory;
        private RequestJsonFactory requestJsonFactory;
        private UploadFileFactory uploadFileFactory;
        private DownLoadFileFactory downLoadFileFactory;

        public Builder() {
            retrofitBuilder = new Retrofit.Builder();
            okHttpClientBuilder = new OkHttpClient.Builder();
        }

        public Builder baseUrl(String url) {
            baseUrl = url;
            return this;
        }

        public Builder timeOut(int TIME_OUT) {
            this.TIME_OUT = TIME_OUT;
            return this;
        }

        public Builder isLog(boolean isLog) {
            this.isLog = isLog;
            return this;
        }

        public Builder addGsonConverterFactory(GsonConverterFactory factory) {
            this.gsonConverterFactory = factory;
            return this;
        }

        public Builder addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory factory) {
            this.javaCallAdapterFactory = factory;
            return this;
        }

        public Builder addRequestJsonFactory(RequestJsonFactory factory) {
            this.requestJsonFactory = factory;
            this.isJson = true;
            return this;
        }

        public Builder addUploadFileFactory(UploadFileFactory factory) {
            this.uploadFileFactory = factory;
            return this;
        }

        public Builder addDownLoadFileFactory(DownLoadFileFactory factory) {
            this.downLoadFileFactory = factory;
            return this;
        }

        public Builder client(OkHttpClient client) {
            this.okHttpClient = client;
            return this;
        }

        public Builder addHeader(HashMap<String, String> headers) {
            okHttpClientBuilder.addInterceptor(new HeaderAddFactory(headers));
            return this;
        }

        public LeopardClient build() {
            if (this.requestJsonFactory != null) {
                okHttpClientBuilder.addInterceptor(this.requestJsonFactory);
            }

            if (this.uploadFileFactory != null) {
                okHttpClientBuilder.addInterceptor(this.uploadFileFactory);
            }

            if (this.downLoadFileFactory != null) {
                okHttpClientBuilder.addInterceptor(this.downLoadFileFactory);
            }

            okHttpClientBuilder.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
            if (isLog)
                okHttpClientBuilder.addNetworkInterceptor(
                        new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS));

            okHttpClient = okHttpClientBuilder.build();

            // retrofit
            if (this.javaCallAdapterFactory != null) {
                retrofitBuilder.addCallAdapterFactory(javaCallAdapterFactory);
            }

            if (this.gsonConverterFactory != null) {
                retrofitBuilder.addConverterFactory(gsonConverterFactory);
            }
            if (baseUrl.startsWith("http"))
                retrofitBuilder.baseUrl(baseUrl);
            retrofitBuilder.client(okHttpClient);
            retrofit = retrofitBuilder.build();

            serverApi = retrofit.create(BaseServerApi.class);

            return new LeopardClient(serverApi, retrofit, retrofitBuilder, okHttpClient, okHttpClientBuilder, isJson);
        }

    }


    private static String fiterURLFromJSON(String params) {
        try {
            JSONObject jsonObject = new JSONObject(params);
            if (jsonObject.has("ruqestURL"))
                jsonObject.remove("ruqestURL");
            return jsonObject.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    private static Map<String, Object> fiterURLFromRequestParams(Map<String, Object> params) {
        if (params.containsKey("ruqestURL"))
            params.remove("ruqestURL");
        return params;
    }

}
