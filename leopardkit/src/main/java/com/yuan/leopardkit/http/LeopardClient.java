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
import com.yuan.leopardkit.http.factory.CacheFactory;
import com.yuan.leopardkit.http.factory.DownLoadFileFactory;
import com.yuan.leopardkit.http.factory.HeaderAddFactory;
import com.yuan.leopardkit.http.factory.RequestComFactory;
import com.yuan.leopardkit.http.factory.RequestJsonFactory;
import com.yuan.leopardkit.http.factory.UploadFileFactory;
import com.yuan.leopardkit.interfaces.FileRespondResult;
import com.yuan.leopardkit.interfaces.HttpRespondResult;
import com.yuan.leopardkit.interfaces.IDownloadProgress;
import com.yuan.leopardkit.interfaces.UploadIProgress;
import com.yuan.leopardkit.servers.BaseServerApi;
import com.yuan.leopardkit.upload.FileUploadEnetity;
import com.yuan.leopardkit.upload.UploadHelper;
import com.yuan.leopardkit.utils.JsonParseUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
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

    public LeopardClient(Context c,BaseServerApi serverApi, Retrofit retrofit, Retrofit.Builder retrofitBuilder, OkHttpClient okHttpClient, OkHttpClient.Builder okHttpClientBuilder, boolean isJson) {
        this.mContext = c;
        this.serverApi = serverApi;
        this.retrofit = retrofit;
        this.retrofitBuilder = retrofitBuilder;
        this.okHttpClient = okHttpClient;
        this.okHttpClientBuilder = okHttpClientBuilder;
        this.isJson = isJson;

    }

    public void POST(BaseEnetity entity, HttpRespondResult callback) {
        //遍历搜索默认拦截器
        for (Interceptor interceptor : okHttpClient.interceptors()){
            if (interceptor instanceof RequestComFactory){
                ((RequestComFactory)(interceptor)).setHttpRespondResult(callback);
                break;
            }
        }
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

    public void GET(BaseEnetity entity, HttpRespondResult callback) {
        //遍历搜索默认拦截器
        for (Interceptor interceptor : okHttpClient.interceptors()){
            if (interceptor instanceof RequestComFactory){
                ((RequestComFactory)(interceptor)).setHttpRespondResult(callback);
                break;
            }
        }
        if (isJson) {
            String json = JsonParseUtil.modeToJson(entity);
            Log.i("yuan", fiterURLFromJSON(json));
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

    public UploadHelper upLoadFiles(final FileUploadEnetity enetity, final UploadIProgress callback) {
        UploadHelper  mUploadHelper = new UploadHelper(serverApi);
        mUploadHelper.upload(enetity,callback);

        return mUploadHelper;
    }

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

        private Context mContext;

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
        private RequestComFactory requestComFactory;
        private CacheFactory cacheFactory;

        public Builder(Context c,String url) {
            retrofitBuilder = new Retrofit.Builder();
            okHttpClientBuilder = new OkHttpClient.Builder();

            initDefalutConfig(url); // 加载默认配置

            this.mContext = c;
            HttpDbUtil.initHttpDB(c);// 尝试初始化数据库
        }

        private void initDefalutConfig(String url){
            baseUrl(url);
            addGsonConverterFactory(GsonConverterFactory.create());
            addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create());
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

//        public Builder addRequestComFactory(RequestComFactory factory){
//            this.requestComFactory = factory;
//            return this;
//        }

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

        public Builder addCacheFactory(CacheFactory factory){
            this.cacheFactory = factory;
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
            //默认第一个添加
            okHttpClientBuilder.addInterceptor(RequestComFactory.create());

            if (this.requestJsonFactory != null) {
                okHttpClientBuilder.addInterceptor(this.requestJsonFactory);
            }

            if (this.uploadFileFactory != null) {
                okHttpClientBuilder.addInterceptor(this.uploadFileFactory);
            }

            if (this.downLoadFileFactory != null) {
                okHttpClientBuilder.addInterceptor(this.downLoadFileFactory);
            }

            if (this.cacheFactory != null){
                okHttpClientBuilder.addInterceptor(this.cacheFactory);
                okHttpClientBuilder.cache(new Cache(this.cacheFactory.getCacheFileDir(),this.cacheFactory.getCacheSize()));
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

            return new LeopardClient(mContext,serverApi, retrofit, retrofitBuilder, okHttpClient, okHttpClientBuilder, isJson);
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
