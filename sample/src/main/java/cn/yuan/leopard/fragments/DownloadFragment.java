package cn.yuan.leopard.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yuan.leopardkit.download.model.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

import cn.yuan.leopard.R;
import cn.yuan.leopard.adapters.DownLoadApater;
import cn.yuan.leopard.model.DownLoadModel;


public class DownloadFragment extends Fragment {

    private RecyclerView recyclerView;
    private DownLoadApater adapter;

    List<DownLoadModel> data = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {

        recyclerView = (RecyclerView) view.findViewById(R.id.down_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new DownLoadApater(data,getActivity());
        recyclerView.setAdapter(adapter);

        initData();
    }

    private void initData(){
        for (int i = 0;i<3;i++){
            String url = "http://a5.pc6.com/pc6_soure/2016-3/cn.wsy.travel.apk";
            DownloadInfo info = new DownloadInfo();
            info.setUrl(url);
            info.setProgress(0l);
            info.setFileName("cn.wsy.travel_"+i+".apk");
            DownLoadModel model = new DownLoadModel();
            model.setInfo(info);
            data.add(model);
        }
        adapter.notifyDataSetChanged();
    }


}
