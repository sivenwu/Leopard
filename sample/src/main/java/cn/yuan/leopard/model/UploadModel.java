package cn.yuan.leopard.model;

import java.io.File;

/**
 * Created by Yuan on 2016/9/2.
 * Detail
 */
public class UploadModel {

    private File file;

    public UploadModel(File file) {
        this.file = file;
    }

    public UploadModel() {
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
