package cn.yuan.leopard.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yuan.leopardkit.LeopardHttp;
import com.yuan.leopardkit.http.base.HttpMethod;
import com.yuan.leopardkit.interfaces.HttpRespondResult;
import com.yuan.leopardkit.interfaces.ILoading;

import java.util.HashMap;

import cn.yuan.leopard.R;
import cn.yuan.leopard.Utils;
import cn.yuan.leopard.model.RequestGetJsonModel;
import cn.yuan.leopard.model.RequestGetModel;
import cn.yuan.leopard.model.RequestPostJsonModel;
import cn.yuan.leopard.model.RequestPostModel;
import okhttp3.Headers;
import okhttp3.internal.framed.Header;


public class RquestFragment extends Fragment implements View.OnClickListener{

    private TextView postTv,postJsonTv,postHeaderTv;
    private TextView getTv,getJsonTv,getHeaderTv;
    private TextView requestTv,requestHeader,resonseData,responseHeader;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        initView(view);
        initListener();
        return view;
    }

    private void initView(View view) {
        postTv = (TextView) view.findViewById(R.id.post_tv);
        postJsonTv = (TextView) view.findViewById(R.id.post_json_tv);
        postHeaderTv = (TextView) view.findViewById(R.id.post_header_tv);

        getTv = (TextView) view.findViewById(R.id.get_tv);
        getJsonTv = (TextView) view.findViewById(R.id.get_json_tv);
        getHeaderTv = (TextView) view.findViewById(R.id.get_header_tv);

        requestTv = (TextView) view.findViewById(R.id.request_state);
        requestHeader = (TextView) view.findViewById(R.id.request_header);
        resonseData = (TextView) view.findViewById(R.id.response_data);
        responseHeader = (TextView) view.findViewById(R.id.response_header);

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
        LeopardHttp.SEND(HttpMethod.POST,getActivity(),new RequestPostModel("leopard", Utils.getNowTime()), new HttpRespondResult(getContext()) {
            @Override
            public void onSuccess(String content) {
                resonseData.setText("onSuccess \n"+content);

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
        LeopardHttp.SEND(HttpMethod.POST_JSON,getActivity(),new RequestPostJsonModel("leopard", Utils.getNowTime()), new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {
                resonseData.setText("onSuccess \n"+content);

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
        LeopardHttp.SEND(HttpMethod.GET,getActivity(),new RequestGetModel("leopard", Utils.getNowTime()), new HttpRespondResult(getContext()) {
            @Override
            public void onSuccess(String content) {
                resonseData.setText("onSuccess \n"+content);

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
        Toast.makeText(getActivity(),"HTTP协议不允许你这么请求喔~",Toast.LENGTH_SHORT).show();
//        LeopardHttp.SEND(HttpMethod.GET_JSON,getActivity(),new RequestGetJsonModel("leopard", Utils.getNowTime()), new HttpRespondResult(getActivity()) {
//            @Override
//            public void onSuccess(String content) {
//                resonseData.setText("onSuccess \n"+content);
//
//                Headers rspHeaders = getResponse().headers();
//                for (int i = 0;i< rspHeaders.size(); i++) {
//                    responseHeader.setText( responseHeader.getText().toString() + rspHeaders.value(i) +"\n");
//                }
//
//                Headers rqHeaders = getRequest().headers();
//                requestTv.setText(getRequest().toString());
//                for (int i = 0;i< rqHeaders.size(); i++) {
//                    requestHeader.setText( responseHeader.getText().toString() + rqHeaders.value(i) +"\n");
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable error, String content) {
//                resonseData.setText("onFailure \n"+content);
//            }
//        });
    }

    public void postHeader(){
        HashMap<String,String> headers = new HashMap<>();
        headers.put("apikey","dqwijotfpgjweigowethiuhqwqpqeqp");
        headers.put("apiSecret","ojtowejioweqwcxzcasdqweqrfrrqrqw");
        headers.put("name","leopard");

        LeopardHttp.SEND(HttpMethod.POST,getActivity(),new RequestPostModel("leopard", Utils.getNowTime()),headers, new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {
                resonseData.setText("onSuccess \n"+content);

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

        LeopardHttp.SEND(HttpMethod.GET,getActivity(),new RequestPostModel("leopard", Utils.getNowTime()),headers, new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {
                resonseData.setText("onSuccess \n"+content);

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
