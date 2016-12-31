package com.yuan.leopardkit;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.http.LeopardClient;
import com.yuan.leopardkit.http.base.BaseEnetity;
import com.yuan.leopardkit.http.base.HttpMethod;
import com.yuan.leopardkit.http.factory.CacheFactory;
import com.yuan.leopardkit.http.factory.RequestComFactory;
import com.yuan.leopardkit.http.factory.RequestJsonFactory;
import com.yuan.leopardkit.http.factory.UploadFileFactory;
import com.yuan.leopardkit.interfaces.FileRespondResult;
import com.yuan.leopardkit.interfaces.HttpRespondResult;
import com.yuan.leopardkit.interfaces.IProgress;
import com.yuan.leopardkit.upload.FileUploadEnetity;

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
     * leopard初始化
     */
    public static void init(Context context) {
        HttpDbUtil.initHttpDB(context.getApplicationContext());
//        registerNetWorkListener(context);
    }

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
        ADDRESS = server;
    }

    private static LeopardClient.Builder getBuilder(Context mC) {
        LeopardClient.Builder builder =  new LeopardClient.Builder()
                .addGsonConverterFactory(GsonConverterFactory.create())
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ADDRESS);

        if (useCache && mC != null){
            builder.addCacheFactory(CacheFactory.create(mC));
        }

        return builder;
    }

    /**
     * 是否使用缓存
     * @param cache
     */
    public static void setUseCache(boolean cache) {
        useCache = cache;
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
     * @param iProgress
     * @return 拥有Task的下载实体
     */
    public static boolean DWONLOAD(final DownloadInfo downloadInfo, final IProgress iProgress) {
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1000) {
                    if (downloadInfo.getState() != DownLoadManager.STATE_WAITING)
                        iProgress.onProgress(msg.arg1, msg.arg2, msg.arg1 >= msg.arg2);
                }else{
                    if (msg.obj !=null && (msg.obj instanceof String))
                    iProgress.onFailed((String) msg.obj);
                }
            }
        };

        return DownLoadManager.getManager().addTask(downloadInfo, new FileRespondResult() {
            @Override
            public void onExecuting(long progress, long total, boolean done) {
                Message message = new Message();
                message.what = 1000;
                message.arg1 = (int) progress;
                message.arg2 = (int) total;
                handler.sendMessageDelayed(message,HANDER_DELAYED_TIME);
            }

            @Override
            public void onFailed(String reason) {
                Message message = new Message();
                message.what = 1001;
                message.obj = reason;
                handler.sendMessageDelayed(message,HANDER_DELAYED_TIME);
            }
        });

    }

    public static void UPLOAD(FileUploadEnetity uploadEnetity, final IProgress iProgress) {
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1000){
                iProgress.onProgress(msg.arg1, msg.arg2, msg.arg1 >= msg.arg2);
                }else{
                    if (msg.obj !=null && (msg.obj instanceof String))
                        iProgress.onFailed((String) msg.obj);
                }
            }
        };

        FileRespondResult respondResult =  new FileRespondResult() {
            @Override
            public void onExecuting(long progress, long total, boolean done) {
                Message message = new Message();
                message.what = 1000;
                message.arg1 = (int) progress;
                message.arg2 = (int) total;
                handler.sendMessageDelayed(message,HANDER_DELAYED_TIME);
            }

            @Override
            public void onFailed(String reason) {
                Message message = new Message();
                message.what = 1001;
                message.obj = reason;
                handler.sendMessageDelayed(message,HANDER_DELAYED_TIME);
            }
        };

        getBuilder(null)
                .addUploadFileFactory(UploadFileFactory.create())
                .build()
                .upLoadFiles(uploadEnetity,respondResult);

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
                .POST(context, enetity, httpRespondResult);
    }

    private static void POST(Context context, BaseEnetity enetity, HashMap<String, String> header, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addHeader(header)
                .build()
                .POST(context, enetity, httpRespondResult);
    }

    private static void POSTJson(Context context, BaseEnetity enetity, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addRequestJsonFactory(RequestJsonFactory.create())
                .build()
                .POST(context, enetity, httpRespondResult);
    }

    private static void POSTJson(Context context, BaseEnetity enetity, HashMap<String, String> header, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addHeader(header)
                .addRequestJsonFactory(RequestJsonFactory.create())
                .build()
                .POST(context, enetity, httpRespondResult);
    }

    private static void GET(Context context, BaseEnetity enetity, HashMap<String, String> header, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addHeader(header)
                .build()
                .GET(context, enetity, httpRespondResult);
    }

    private static void GET(Context context, BaseEnetity enetity, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .build()
                .GET(context, enetity, httpRespondResult);
    }

    private static void GETjson(Context context, BaseEnetity enetity, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addRequestJsonFactory(RequestJsonFactory.create())
                .build()
                .GET(context, enetity, httpRespondResult);
    }

    private static void GETjson(Context context, BaseEnetity enetity, HashMap<String, String> header, HttpRespondResult httpRespondResult) {
        getBuilder(context)
                .addHeader(header)
                .addRequestJsonFactory(RequestJsonFactory.create())
                .build()
                .GET(context, enetity, httpRespondResult);
    }

}
