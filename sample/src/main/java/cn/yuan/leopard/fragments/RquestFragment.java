package cn.yuan.leopard.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.yuan.leopard.R;


public class RquestFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        TextView content = (TextView) view.findViewById(R.id.content);
        content.setText("傻小孩b Fragment one");
    }


}
