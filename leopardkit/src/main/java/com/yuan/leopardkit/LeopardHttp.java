package com.yuan.leopardkit;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.http.LeopardClient;
import com.yuan.leopardkit.http.base.BaseEnetity;
import com.yuan.leopardkit.http.base.HttpMethod;
import com.yuan.leopardkit.http.factory.CacheFactory;
import com.yuan.leopardkit.http.factory.RequestJsonFactory;
import com.yuan.leopardkit.http.factory.UploadFileFactory;
import com.yuan.leopardkit.interfaces.HttpRespondResult;
import com.yuan.leopardkit.interfaces.IDownloadProgress;
import com.yuan.leopardkit.interfaces.UploadIProgress;
import com.yuan.leopardkit.upload.FileUploadEnetity;
import com.yuan.leopardkit.upload.UploadHelper;

import java.util.HashMap;

import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Yuan on 2016/9/1.
 * Detail 自动化构建请求对象 开发者也可以自定义
 */
public class LeopardHttp {

    private static final String TAG = "LeopardHttp";

    private static int HANDER_DELAYED_TIME = 500;
    private static String ADDRESS = "http://127.0.0.1";

    private static boolean useCache = false;

    /**
     * 绑定主机域名
     * @param server 服务器域名
     * @param port 服务器端口
     */
    public static void bindServer(String server,int port){
        ADDRESS = port==-1?server+""+port : server;
    }

    /**
     * 绑定主机域名 默认80端口
     * @param server 服务器域名
     */
    public static void bindServer(String server){
        if (!server.endsWith("/")){
            server = server + "/";
        }
        bindServer(server,80);
    }

    /**
     * 是否使用缓存
     * @param cache
     */
    public static void setUseCache(boolean cache) {
        useCache = cache;
    }

    private static LeopardClient.Builder getBuilder(Context mC) {
        LeopardClient.Builder builder =  new LeopardClient.Builder(mC,ADDRESS);

        if (useCache && mC != null){
            builder.addCacheFactory(CacheFactory.create(mC));
        }

        return builder;
    }

    private static void registerNetWorkListener(Context context){
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        NetWorkStateReceiver mReceiver = new NetWorkStateReceiver();
        mReceiver.setNetWorkStateListenerl(new NetWorkStateReceiver.NetWorkStateListener() {
            @Override
            public void disConnect() {
                DownLoadManager.getManager().pauseAllTask();
            }

            @Override
            public void resumeConnect() {
                DownLoadManager.getManager().startAllTask();
            }
        });

        context.registerReceiver(mReceiver, filter);
    }

    /**
     * 提供不需要自定义头部入口
     *
     * @param type
     * @param enetity
     * @param httpRespondResult
     */
    public static void SEND(HttpMethod type, Context context, BaseEnetity enetity, HttpRespondResult httpRespondResult) {
        if (type.toString().equals(HttpMethod.GET.toString())) {
            GET(context, enetity, httpRespondResult);
        } else if (type.toString().equals(HttpMethod.GET_JSON.toString())) {
            GETjson(context, enetity, httpRespondResult);
        } else if (type.toString().equals(HttpMethod.POST.toString())) {
            POST(context, enetity, httpRespondResult);
        } else if (type.toString().equals(HttpMethod.POST_JSON.toString())) {
            POSTJson(context, enetity, httpRespondResult);
        } else {
            Log.i(TAG, "Type unknown");
        }
    }

    /**
     * 下载入口
     *
     * @param downloadInfo
     * @param iDownloadProgress
     * @return 拥有Task的下载实体
     */
    public static long DWONLOAD(final DownloadInfo downloadInfo, final IDownloadProgress iDownloadProgress) {
        return DownLoadManager.getManager().addTask(downloadInfo,iDownloadProgress);
    }

    /**
     * 支持返回Uploadhelper 可以close
     * @param uploadEnetity
     * @param iProgress
     * @return
     */
    public static UploadHelper UPLOAD(FileUploadEnetity uploadEnetity, final UploadIProgress iProgress) {
        return getBuilder(null)
                .addUploadFileFactory(UploadFileFactory.create())
                .build()
                .upLoadFiles(uploadEnetity,iProgress);
    }

    /**
     * 提供需要自定义头部入口
     *
     * @param type
     * @param context
     * @param enetity
     * @param header
     * @param httpRespondResult
     */
    public static void SEND(HttpMethod type, Context context, BaseEnetity enetity, HashMap<String, String> header, HttpRespondResult httpRespondResult) {
        if (type.toString().equals(HttpMethod.GET.toString())) {
            GET(context, enetity, header, httpRespondResult);
        } else if (type.toString().equals(HttpMethod.GET_JSON.toString())) {
            GETjson(context, enetity, header, httpRespondResult);
        } else if (type.toString().equals(HttpMethod.POST.toString())) {
            POST(context, enetity, header, httpRespondResult);
        } else if (type.toString().equals(HttpMethod.POST_JSON.toString())) {
            POSTJson(context, enetity, header, httpRespondResult);
        } else {
            Log.i(TAG, "Type unknown");
        }
    }


    private static void POST(Context context, BaseEnetity enetity, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .build()
                .POST(enetity, httpRespondResult);
    }

    private static void POST(Context context, BaseEnetity enetity, HashMap<String, String> header, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addHeader(header)
                .build()
                .POST(enetity, httpRespondResult);
    }

    private static void POSTJson(Context context, BaseEnetity enetity, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addRequestJsonFactory(RequestJsonFactory.create())
                .build()
                .POST(enetity, httpRespondResult);
    }

    private static void POSTJson(Context context, BaseEnetity enetity, HashMap<String, String> header, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addHeader(header)
                .addRequestJsonFactory(RequestJsonFactory.create())
                .build()
                .POST( enetity, httpRespondResult);
    }

    private static void GET(Context context, BaseEnetity enetity, HashMap<String, String> header, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addHeader(header)
                .build()
                .GET(enetity, httpRespondResult);
    }

    private static void GET(Context context, BaseEnetity enetity, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .build()
                .GET(enetity, httpRespondResult);
    }

    private static void GETjson(Context context, BaseEnetity enetity, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addRequestJsonFactory(RequestJsonFactory.create())
                .build()
                .GET(enetity, httpRespondResult);
    }

    private static void GETjson(Context context, BaseEnetity enetity, HashMap<String, String> header, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addHeader(header)
                .addRequestJsonFactory(RequestJsonFactory.create())
                .build()
                .GET( enetity, httpRespondResult);
    }

}
