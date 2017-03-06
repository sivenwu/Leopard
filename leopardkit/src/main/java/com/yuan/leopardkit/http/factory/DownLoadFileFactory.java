package com.yuan.leopardkit.http.factory;

import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownLoadResponseBody;
import com.yuan.leopardkit.download.model.DownloadInfo;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Yuan on 2016/8/26.
 * Detail 下載攔截器
 */
public class DownLoadFileFactory implements Interceptor {

//    private FileRespondResult fileRespondResult;
    // 1.4 版本更改为内部回调
    private DownLoadResponseBody.DownLoadBodyListener downLoadBodyListener;
    private DownloadInfo downloadInfo;

    public DownLoadFileFactory() {
    }

    public DownLoadFileFactory(DownLoadResponseBody.DownLoadBodyListener downLoadBodyListener, DownloadInfo downloadInfo) {
        this.downLoadBodyListener = downLoadBodyListener;
        this.downloadInfo = downloadInfo;
    }

    public void setDownloadInfo(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    public void setFileRespondResult(DownLoadResponseBody.DownLoadBodyListener downLoadBodyListener) {
        this.downLoadBodyListener = downLoadBodyListener;
    }

    @Override
    public Response intercept(Chain chain) {
        Request request = chain.request().newBuilder().addHeader("RANGE", "bytes=" + downloadInfo.getBreakProgress() + "-").build();
        Response originalResponse = null;
        try {
            originalResponse = chain.proceed(request);
        } catch (IOException e) {
            DownLoadManager.getManager().pauseTask(this.downloadInfo);
            this.downLoadBodyListener.onFailed(e.getMessage().toString());
            e.printStackTrace();

            return originalResponse;
        }
        DownLoadResponseBody body = new DownLoadResponseBody(this.downloadInfo ,originalResponse.body(), this.downLoadBodyListener);
        Response response = originalResponse.newBuilder().body(body).build();
        return response;
    }

    public static DownLoadFileFactory create() {
        return new DownLoadFileFactory();
    }

    public static DownLoadFileFactory create(DownLoadResponseBody.DownLoadBodyListener downLoadBodyListener, DownloadInfo downloadInfo) {
        return new DownLoadFileFactory(downLoadBodyListener, downloadInfo);
    }

}
