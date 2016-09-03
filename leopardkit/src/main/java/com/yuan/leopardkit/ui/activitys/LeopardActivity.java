package com.yuan.leopardkit.ui.activitys;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.yuan.leopardkit.interfaces.ILoading;

/**
 * Created by Yuan on 2016/9/1.
 * Detail 可以直接继承这个类,向外抛出加载接口(默认ProgressDialog加载)
 */
public class LeopardActivity extends AppCompatActivity implements ILoading {

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initProgress();
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    public void onStartLoading(String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    @Override
    public void onFinshLoading() {
        progressDialog.dismiss();
    }
}
