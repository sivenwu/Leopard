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

    public DownLoadFileFactory() {
    }

    public DownLoadFileFactory(FileRespondResult fileRespondResult, DownloadInfo downloadInfo) {
        this.fileRespondResult = fileRespondResult;
        this.downloadInfo = downloadInfo;
    }

    public void setDownloadInfo(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    public void setFileRespondResult(FileRespondResult fileRespondResult) {
        this.fileRespondResult = fileRespondResult;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder().addHeader("RANGE", "bytes=" + downloadInfo.getBreakProgress() + "-").build();
        Response originalResponse = chain.proceed(request);
        DownLoadResponseBody body = new DownLoadResponseBody(this.downloadInfo ,originalResponse.body(), fileRespondResult);
        Response response = originalResponse.newBuilder().body(body).build();
        return response;
    }

    public static DownLoadFileFactory create() {
        return new DownLoadFileFactory();
    }

    public static DownLoadFileFactory create(FileRespondResult fileRespondResult, DownloadInfo downloadInfo) {
        return new DownLoadFileFactory(fileRespondResult, downloadInfo);
    }

}
