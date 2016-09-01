package com.yuan.leopardkit.servers;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by Yuan on 2016/8/23.
 * Detail 默认基础的api 提供 get post download upload
 */
public interface BaseServerApi {

    @POST("{path}")
    Observable<ResponseBody> post(
            @Path(value = "path", encoded = true) String path,
            @QueryMap Map<String, Object> map);

    @GET("{path}")
    Observable<ResponseBody> get(
            @Path(value = "path", encoded = true) String path,
            @QueryMap Map<String, Object> map);

    @POST("{path}")
    Observable<ResponseBody> postJSON(
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody route);

    @GET("{path}")
    Observable<ResponseBody> getJSON(
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody route);

//    统一用下面那个上传入口
//    @Multipart
//    @POST("{path}")
//    Observable<ResponseBody> upLoadFile(
//            @Path(value = "path",encoded = true) String url,
//            @Part("image\"; filename=\"image.jpg") RequestBody requestBody);


    @Multipart
    @POST("{path}")
    Observable<ResponseBody> uploadFile(
            @Path(value = "path", encoded = true) String url,
            @PartMap() Map<String, RequestBody> maps);

    //支持大文件
//    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(
            @Url String fileUrl);


}
