package com.yuan.leopardkit.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuan on 2016/8/24.
 * Detail 文件上传包装类
 */
public class FileUploadEnetity {

    long size = 0;
    private String url;
    private List<File> files = new ArrayList<>();

    public FileUploadEnetity(String url, File file) {
        this.url = url;
        this.files.add(file);
        initSize();
    }

    public FileUploadEnetity(String url, List<File> files) {
        this.url = url;
        this.files = files;
        initSize();
    }

    private void initSize(){
        for (File file : files) {
            size += file.length();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getSimpleFile() {
        return files.get(0);
    }

    public List<File> getFiles() {
        return files;
    }

    public long getFilesTotalSize() {
         return size;
    }
}
