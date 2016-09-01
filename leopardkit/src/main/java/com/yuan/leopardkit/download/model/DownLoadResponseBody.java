package com.yuan.leopardkit.download.model;

import com.yuan.leopardkit.interfaces.FileRespondResult;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by Yuan on 2016/8/25.
 * Detail 下载响应监听进度
 */
public class DownLoadResponseBody extends ResponseBody {

    private ResponseBody mResponseBody;
    private FileRespondResult fileRespondResult;
    private BufferedSource bufferedSource;

    public DownLoadResponseBody(ResponseBody mResponseBody) {
        this.mResponseBody = mResponseBody;
    }

    public DownLoadResponseBody(ResponseBody mResponseBody, FileRespondResult mProgressListener) {
        this.mResponseBody = mResponseBody;
        this.fileRespondResult = mProgressListener;
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return bufferedSource;
    }

    public Source source(Source source) {
        return new ForwardingSource(source) {

            //当前读取字节数
            long bytesRead = 0L;
            //总字节长度
            long totalLength = 0L;

            int i = 0;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                //读取的字节数
                try {
                    bytesRead = super.read(sink, byteCount);
                } catch (Exception e){
                    e.printStackTrace();
                }
                if (bytesRead != -1) {

                } else {
                    bytesRead = 0;
                }
                totalLength += bytesRead;
                fileRespondResult.onExecuting(totalLength, mResponseBody.contentLength(), totalLength==mResponseBody.contentLength());
                return bytesRead;
            }
        };
    }
}
