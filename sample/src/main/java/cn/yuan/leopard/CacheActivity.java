package cn.yuan.leopard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yuan.leopardkit.LeopardHttp;
import com.yuan.leopardkit.http.LeopardClient;
import com.yuan.leopardkit.http.base.HttpMethod;
import com.yuan.leopardkit.http.factory.CacheFactory;
import com.yuan.leopardkit.http.factory.RequestComFactory;
import com.yuan.leopardkit.http.factory.RequestJsonFactory;
import com.yuan.leopardkit.interfaces.HttpRespondResult;
import com.yuan.leopardkit.ui.activitys.LeopardActivity;
import com.yuan.leopardkit.utils.NetWorkUtil;

import java.util.HashMap;

import cn.yuan.leopard.model.RequestGetModel;
import cn.yuan.leopard.model.RequestPostJsonModel;
import cn.yuan.leopard.model.RequestPostModel;
import okhttp3.Headers;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CacheActivity extends LeopardActivity implements View.OnClickListener{

    private TextView postTv,postJsonTv,postHeaderTv;
    private TextView getTv,getJsonTv,getHeaderTv;
    private TextView requestTv,requestHeader,resonseData,responseHeader;

    private String url = "http://wxwusy.applinzi.com/leopardWeb/app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache);
        initView();
        initListener();

    }

    private LeopardClient.Builder getClient(){

        LeopardClient.Builder builder = new LeopardClient.Builder()
                .addGsonConverterFactory(GsonConverterFactory.create())
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addCacheFactory(CacheFactory.create(this))
                .baseUrl(url);
        return builder;
    }

    private void initView() {
        postTv = (TextView) findViewById(R.id.post_tv);
        postJsonTv = (TextView) findViewById(R.id.post_json_tv);
        postHeaderTv = (TextView) findViewById(R.id.post_header_tv);

        getTv = (TextView)findViewById(R.id.get_tv);
        getJsonTv = (TextView)findViewById(R.id.get_json_tv);
        getHeaderTv = (TextView)findViewById(R.id.get_header_tv);

        requestTv = (TextView)findViewById(R.id.request_state);
        requestHeader = (TextView)findViewById(R.id.request_header);
        resonseData = (TextView)findViewById(R.id.response_data);
        responseHeader = (TextView)findViewById(R.id.response_header);


    }

    private void initListener(){
        postTv.setOnClickListener(this);
        postJsonTv.setOnClickListener(this);
        postHeaderTv.setOnClickListener(this);
        getTv.setOnClickListener(this);
        getJsonTv.setOnClickListener(this);
        getHeaderTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        requestHeader.setText("--");
        responseHeader.setText("--");
        switch (v.getId()){
            case R.id.post_tv:
                post();
                break;
            case R.id.post_json_tv:
                postjson();
                break;
            case R.id.post_header_tv:
                postHeader();
                break;
            case R.id.get_tv:
                get();
                break;
            case R.id.get_json_tv:
                getJson();
                break;
            case R.id.get_header_tv:
                getHeader();
                break;
        }
    }


    public void post(){
        getClient().build().POST(this, new RequestPostModel("leopard", "QQ 450302004"), new HttpRespondResult() {
            @Override
            public void onSuccess(String content) {
                if (NetWorkUtil.isNetworkAvailable(CacheActivity.this)) {
                    resonseData.setText("onSuccess \n" + content);
                }else {
                    resonseData.setText("缓存 ：onSuccess \n" + content);
                }

                Headers rspHeaders = getResponse().headers();
                for (int i = 0;i< rspHeaders.size(); i++) {
                    responseHeader.setText( responseHeader.getText().toString() + rspHeaders.name(i)
                            +" : "+rspHeaders.value(i) +"\n");
                }

                Headers rqHeaders = getRequest().headers();
                requestTv.setText(getRequest().toString());
                for (int i = 0;i< rqHeaders.size(); i++) {
                    requestHeader.setText( responseHeader.getText().toString() + rqHeaders.name(i)
                            +" : "+rqHeaders.value(i) +"\n");
                }
            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
    }

    public void postjson(){
        getClient().addRequestJsonFactory(RequestJsonFactory.create()).build()
                .POST(this, new RequestPostJsonModel("leopard", "QQ 450302004"), new HttpRespondResult() {
                    @Override
                    public void onSuccess(String content) {
                        if (NetWorkUtil.isNetworkAvailable(CacheActivity.this)) {
                            resonseData.setText("onSuccess \n" + content);
                        }else {
                            resonseData.setText("缓存 ：onSuccess \n" + content);
                        }

                        Headers rspHeaders = getResponse().headers();
                        for (int i = 0;i< rspHeaders.size(); i++) {
                            responseHeader.setText( responseHeader.getText().toString() + rspHeaders.name(i)
                                    +" : "+rspHeaders.value(i) +"\n");
                        }

                        Headers rqHeaders = getRequest().headers();
                        requestTv.setText(getRequest().toString());
                        for (int i = 0;i< rqHeaders.size(); i++) {
                            requestHeader.setText( responseHeader.getText().toString() + rqHeaders.name(i)
                                    +" : "+rqHeaders.value(i) +"\n");
                        }
                    }

                    @Override
                    public void onFailure(Throwable error, String content) {
                        resonseData.setText("onFailure \n"+content);
                    }
                });
    }

    public void get(){
        getClient().build().GET(this, new RequestGetModel("leopard", "QQ 450302004"), new HttpRespondResult() {
            @Override
            public void onSuccess(String content) {
                if (NetWorkUtil.isNetworkAvailable(CacheActivity.this)) {
                    resonseData.setText("onSuccess \n" + content);
                }else {
                    resonseData.setText("缓存 ：onSuccess \n" + content);
                }

                Headers rspHeaders = getResponse().headers();
                for (int i = 0;i< rspHeaders.size(); i++) {
                    responseHeader.setText( responseHeader.getText().toString() + rspHeaders.name(i)
                            +" : "+rspHeaders.value(i) +"\n");
                }

                Headers rqHeaders = getRequest().headers();
                requestTv.setText(getRequest().toString());
                for (int i = 0;i< rqHeaders.size(); i++) {
                    requestHeader.setText( responseHeader.getText().toString() + rqHeaders.name(i)
                            +" : "+rqHeaders.value(i) +"\n");
                }
            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
    }

    public void getJson(){
        Toast.makeText(this,"HTTP协议不允许你这么请求喔~",Toast.LENGTH_SHORT).show();
    }

    public void postHeader(){
        HashMap<String,String> headers = new HashMap<>();
        headers.put("apikey","dqwijotfpgjweigowethiuhqwqpqeqp");
        headers.put("apiSecret","ojtowejioweqwcxzcasdqweqrfrrqrqw");
        headers.put("name","leopard");

        getClient().addHeader(headers)
                .build()
                .POST(this, new RequestPostModel("leopard", "QQ 450302004"), new HttpRespondResult() {
                    @Override
                    public void onSuccess(String content) {
                        if (NetWorkUtil.isNetworkAvailable(CacheActivity.this)) {
                            resonseData.setText("onSuccess \n" + content);
                        }else {
                            resonseData.setText("缓存 ：onSuccess \n" + content);
                        }

                        Headers rspHeaders = getResponse().headers();
                        for (int i = 0;i< rspHeaders.size(); i++) {
                            responseHeader.setText( responseHeader.getText().toString() + rspHeaders.name(i)
                                    +" : "+rspHeaders.value(i) +"\n");
                        }

                        Headers rqHeaders = getRequest().headers();
                        requestTv.setText(getRequest().toString());
                        for (int i = 0;i< rqHeaders.size(); i++) {
                            requestHeader.setText( responseHeader.getText().toString() + rqHeaders.name(i)
                                    +" : "+rqHeaders.value(i) +"\n");
                        }
                    }

                    @Override
                    public void onFailure(Throwable error, String content) {
                        resonseData.setText("onFailure \n"+content);
                    }
                });
    }

    public void getHeader(){
        HashMap<String,String> headers = new HashMap<>();
        headers.put("apikey","getjiocxcpkpjiojojopdfjsd");
        headers.put("apiSecret","uoivhqpgbdljv8iqfhoprf");
        headers.put("name","leopard");

        getClient().addHeader(headers)
                .build()
                .GET(this,new RequestPostModel("leopard", "QQ 450302004"), new HttpRespondResult() {
                    @Override
                    public void onSuccess(String content) {
                        if (NetWorkUtil.isNetworkAvailable(CacheActivity.this)) {
                            resonseData.setText("onSuccess \n" + content);
                        }else {
                            resonseData.setText("缓存 ：onSuccess \n" + content);
                        }

                        Headers rspHeaders = getResponse().headers();
                        for (int i = 0;i< rspHeaders.size(); i++) {
                            responseHeader.setText( responseHeader.getText().toString() + rspHeaders.name(i)
                                    +" : "+rspHeaders.value(i) +"\n");
                        }

                        Headers rqHeaders = getRequest().headers();
                        requestTv.setText(getRequest().toString());
                        for (int i = 0;i< rqHeaders.size(); i++) {
                            requestHeader.setText( responseHeader.getText().toString() + rqHeaders.name(i)
                                    +" : "+rqHeaders.value(i) +"\n");
                        }
                    }

                    @Override
                    public void onFailure(Throwable error, String content) {
                        resonseData.setText("onFailure \n"+content);
                    }
                });
    }
}
