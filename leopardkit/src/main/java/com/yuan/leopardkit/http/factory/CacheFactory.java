package com.yuan.leopardkit.http.factory;

import android.content.Context;
import android.util.Log;

import com.yuan.leopardkit.models.CacheModel;
import com.yuan.leopardkit.utils.CacheHelper;
import com.yuan.leopardkit.utils.NetWorkUtil;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * Created by Yuan on 2016/10/30.
 * Detail cache for http 如果离线缓存与在线缓存同时使用，在线的时候必须先将离线缓存清空
 */

public class CacheFactory implements Interceptor {

    private final String TAG = "CacheFactory";

    private Context mC;
    private CacheHelper cacheHelper;
    private boolean isOnlineCache = false;//是否支持在线缓存

    //http method
    private final String HTTP_GET = "GET";
    private final String HTTP_POST = "POST";

    //vaild time
    private int ONLINE_VAILD_TIME = 60;//默认在线缓存时间是一分钟

    public CacheFactory(Context mC) {
        this.mC = mC;
        cacheHelper = new CacheHelper(mC);
    }

    public CacheFactory(Context mC, boolean isOnlineCache) {
        this.mC = mC;
        this.isOnlineCache = isOnlineCache;
        cacheHelper = new CacheHelper(mC);
    }

    public CacheFactory(Context mC, boolean isOnlineCache, int online_time) {
        this.mC = mC;
        this.isOnlineCache = isOnlineCache;
        this.ONLINE_VAILD_TIME = online_time;
        cacheHelper = new CacheHelper(mC);
    }

    public File getCacheFileDir(){
        return cacheHelper.getCacheFile(1);
    }

    public long getCacheSize(){
        return cacheHelper.getCacheSize();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = null;
        Response response = null;

        request = chain.request();
        if (request.method().equals(HTTP_GET)) {

            response = cacheForGet(request, chain);

        } else if (request.method().equals(HTTP_POST)) {

            response = cacheForPost(request,chain);

        }

        return response;
    }

    public static CacheFactory create(Context mC) {
        return new CacheFactory(mC);
    }
    public static CacheFactory create(Context mC, boolean isOnlineCache) {
        return new CacheFactory(mC,isOnlineCache);
    }
    public static CacheFactory create(Context mC, boolean isOnlineCache, int online_time) {
        return new CacheFactory(mC,isOnlineCache,online_time);
    }

    private Response actionOnlineDisplay(Request request,Chain chain) throws IOException {
        Response response = null;
        String url = request.url().toString();

        Log.d(TAG,"[ONLINE CACHE]"+" 请求支持在线缓存");
        CacheModel model = cacheHelper.getCache(url);
        if (model == null || model.getResponseData() == null || model.getHeaders() == null){
            Log.d(TAG,"[ONLINE CACHE]"+"暂时没有缓存，进行数据缓存中...");
            response = onlineCacheGetCache(request,chain);
        }else{
            Log.d(TAG,"[ONLINE CACHE]"+"缓存存在，进行读取中...");
            CacheModel cache_model = cacheHelper.getCache(url);
            if (model.getResponseData() != null){
                Response.Builder builder = new Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_0)// TODO: 2016/10/31 待验证
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + CacheHelper.VALID_TIME)
                        .removeHeader("Pragma")
                        .code(200)
                        .body(ResponseBody.create(null,cache_model.getResponseData()));

                String[] strHeaders = new String(cache_model.getHeaders(),CacheHelper.CHARSET_NAME).split("\n");
                for (String header : strHeaders){
                    String[] sim_header = header.split(":");
                    builder.addHeader(sim_header[0],sim_header[1]);

                    if (sim_header[0].equals("leopard_type")){
                        if (sim_header[1].equals(CacheHelper.TYPE_OFFLINE +"")){//如果是离线缓存，这时候需要重新clean之前的缓存，
                            Log.d(TAG,"[ONLINE CACHE]"+"缓存是离线缓存，重新获取中...");
                            response = onlineCacheGetCache(request,chain);//重新获取覆盖即可
                            return response;
                        }
                    }
                }

                response = builder.build();
            }
        }
        Log.d(TAG,"[ONLINE CACHE]"+"缓存是在线缓存，获取中...");
        return response;
    }

    private Response onlineCacheGetCache(Request request,Chain chain) throws IOException {
        Response response =  chain.proceed(request);
        String url = request.url().toString();

        //display data
        BufferedSource bs = response.body().source();
        bs.request(Long.MAX_VALUE); // Buffer the entire body.
        okio.Buffer buffer = bs.buffer();
        byte[] data = buffer.clone().readByteArray();

        //display header
        String headers = response.headers().toString();

        //recored type
        headers  += "leopard_type:"+ CacheHelper.TYPE_ONLINE +"\n";

        //model
        CacheModel online_model = new CacheModel(headers.getBytes(), new String(data, CacheHelper.CHARSET_NAME).getBytes());
        online_model.setType(CacheHelper.TYPE_ONLINE);

        if (buffer.size() > 0) {
            cacheHelper.putCache(url, online_model,ONLINE_VAILD_TIME);
        }

        return response;
    }

    private Response cacheForGet(Request request, Chain chain) throws IOException {
        if (!NetWorkUtil.isNetworkAvailable(this.mC)) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }

        Response response = chain.proceed(request);
        if (NetWorkUtil.isNetworkAvailable(this.mC)) {
            if (isOnlineCache){//如果支持在线缓存
                response = response.newBuilder()
                        .header("Cache-Control", "public, max-age=" + ONLINE_VAILD_TIME)
                        .removeHeader("Pragma")////清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .build();
            }else{
                response = response.newBuilder()
                        .header("Cache-Control", "public, max-age=" + 0)//不支持直接时间0
                        .removeHeader("Pragma")
                        .build();
            }

        } else {
            response = response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + CacheHelper.VALID_TIME)
                    .removeHeader("Pragma")
                    .build();
        }
        return response;
    }

    private Response cacheForPost(Request request, Chain chain) throws IOException {

        Response response = null;

        if (!NetWorkUtil.isNetworkAvailable(this.mC)) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }

        if (!isOnlineCache)
            response = chain.proceed(request);

        String url = request.url().toString();

        if (NetWorkUtil.isNetworkAvailable(this.mC)){

            if (!isOnlineCache) {
                //display data
                BufferedSource bs = response.body().source();
                bs.request(Long.MAX_VALUE); // Buffer the entire body.
                okio.Buffer buffer = bs.buffer();
                byte[] data = buffer.clone().readByteArray();

                //display header
                String headers = response.headers().toString();

                //recored type
                headers  += "leopard_type:"+ CacheHelper.TYPE_OFFLINE +"\n";

                //model
                CacheModel model = new CacheModel(headers.getBytes(), new String(data, CacheHelper.CHARSET_NAME).getBytes());
                model.setType(CacheHelper.TYPE_OFFLINE);

                if (buffer.size() > 0) {
                    cacheHelper.putCache(url, model);
                }
            }else{
                // TODO: 2016/10/31 处理在线缓存
                response =  actionOnlineDisplay(request,chain);
            }

        }else{//没有网络的时候
            MediaType contentType = response.body().contentType();
            CacheModel model = cacheHelper.getCache(url);
            if (model.getResponseData() != null){
                Response.Builder builder = response.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + CacheHelper.VALID_TIME)
                        .removeHeader("Pragma")
                        .code(200)
                        .body(ResponseBody.create(contentType,model.getResponseData()));

                String[] strHeaders = new String(model.getHeaders(),CacheHelper.CHARSET_NAME).split("\n");
                for (String header : strHeaders){
                    String[] sim_header = header.split(":");
                    builder.addHeader(sim_header[0],sim_header[1]);
                }

                response = builder.build();
            }

        }

        return response;
    }

}
