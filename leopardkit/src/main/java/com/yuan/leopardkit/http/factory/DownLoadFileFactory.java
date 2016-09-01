package com.yuan.leopardkit.http.factory;

import com.yuan.leopardkit.download.model.DownLoadResponseBody;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.interfaces.FileRespondResult;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Yuan on 2016/8/26.
 * Detail 下載攔截器
 */
public class DownLoadFileFactory implements Interceptor {

    private FileRespondResult fileRespondResult;
    private DownloadInfo downloadInfo;
    private long startPoints = 0L;

    public DownLoadFileFactory() {
    }

    public DownLoadFileFactory(FileRespondResult fileRespondResult, DownloadInfo downloadInfo, long startPoints) {
        this.fileRespondResult = fileRespondResult;
        this.downloadInfo = downloadInfo;
        this.startPoints = startPoints;
    }

    public void setDownloadInfo(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    public void setFileRespondResult(FileRespondResult fileRespondResult) {
        this.fileRespondResult = fileRespondResult;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder().addHeader("RANGE", "bytes=" + startPoints + "-").build();
        Response originalResponse = chain.proceed(request);
        DownLoadResponseBody body = new DownLoadResponseBody(originalResponse.body(), fileRespondResult);
        Response response = originalResponse.newBuilder().body(body).build();
        return response;
    }

    public static DownLoadFileFactory create() {
        return new DownLoadFileFactory();
    }

    public static DownLoadFileFactory create(FileRespondResult fileRespondResult, DownloadInfo downloadInfo, long startPoints) {
        return new DownLoadFileFactory(fileRespondResult, downloadInfo, startPoints);
    }

}
