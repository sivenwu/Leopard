package cn.yuan.leopard.fragments;


import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.yuan.leopardkit.db.HttpDbUtil;
import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

import cn.yuan.leopard.R;
import cn.yuan.leopard.adapters.DownLoadApater;
import cn.yuan.leopard.model.DownLoadModel;


public class DownloadFragment extends Fragment implements View.OnClickListener {

    private RecyclerView recyclerView;
    private DownLoadApater adapter;

    private TextView pathShowTv;
    private Button deleteAllBtn, stopAllBtn, startAllBtn, pauseAllBtn;

    List<DownLoadModel> data = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        initView(view);
        initListener();
        return view;
    }

    private void initView(View view) {

        pathShowTv = (TextView) view.findViewById(R.id.down_path_tv);
        pathShowTv.setText(pathShowTv.getText().toString()+" "+DownLoadManager.getManager().deFaultDir);
        startAllBtn = (Button) view.findViewById(R.id.down_start_all_btn);
        stopAllBtn = (Button) view.findViewById(R.id.down_stop_all_btn);
        pauseAllBtn = (Button) view.findViewById(R.id.down_pause_all_btn);
        deleteAllBtn = (Button) view.findViewById(R.id.down_delete_all_btn);

        recyclerView = (RecyclerView) view.findViewById(R.id.down_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new DownLoadApater(data, getActivity());
        recyclerView.setAdapter(adapter);

        initData();
    }

    private void initListener() {
        startAllBtn.setOnClickListener(this);
        stopAllBtn.setOnClickListener(this);
        pauseAllBtn.setOnClickListener(this);
        deleteAllBtn.setOnClickListener(this);
    }

    private void initData() {

        List<DownloadInfo> downloadInfoList =   HttpDbUtil.instance.queryFileInfo(0);

        for (DownloadInfo info:downloadInfoList){
            DownLoadModel model = new DownLoadModel();
            model.setInfo(info);
            data.add(model);
        }

        if (data.size() <=0) {
        for (int i = 0; i < 3; i++) {
            String url = "http://f1.market.xiaomi.com/download/AppStore/03f82a470d7ac44300d8700880584fe856387aac6/cn.wsy.travel.apk";
            DownloadInfo info = new DownloadInfo();
            info.setUrl(url);
            info.setFileSavePath(Environment.getExternalStorageDirectory() + "/AAADwonload/");// 自定义下载路径
            info.setFileName("IRecord_" + i + ".apk");
            DownLoadModel model = new DownLoadModel();
            model.setInfo(info);
            data.add(model);
        }
        }
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.down_start_all_btn:
                DownLoadManager.getManager().startAllTask();
                break;

            case R.id.down_delete_all_btn:
                DownLoadManager.getManager().removeAllTask();
                data.clear();
                adapter.notifyDataSetChanged();
                break;

            case R.id.down_pause_all_btn:
                DownLoadManager.getManager().pauseAllTask();
                break;

            case R.id.down_stop_all_btn:
                DownLoadManager.getManager().stopAllTask();
                break;
        }
    }
}
