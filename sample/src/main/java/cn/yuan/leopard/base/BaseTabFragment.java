package cn.yuan.leopard.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.yuan.leopard.R;


/**
 * 应用在需要多个fragment场景,ViewPager作为存储容器 viewpager依赖父类
 * Created by wsy on 2016/8/15.
 */
public abstract class BaseTabFragment extends BaseFragment{

    private ViewPager viewPager;
    private List<ViewPageInfo> fragList;
    protected FragmentStatePagerAdapter mAdapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);

        if (mAdapter == null){
            fragList = new ArrayList<>();
            addSubViewTab();
            mAdapter = new FragmentStatePagerAdapter(getFragmentManager()) {
                @Override
                public Fragment getItem(int position) {
                    return fragList.get(position).fragment;
                }

                @Override
                public int getCount() {
                    return fragList.size();
                }

                @Override
                public CharSequence getPageTitle(int position) {
                    return fragList.get(position).tag;
                }


            };
            if (viewPager !=null) {
                viewPager.setAdapter(mAdapter);
                loadFinishView(viewPager, mAdapter);
            }

        }else{
            if (viewPager !=null) {
                viewPager.setAdapter(mAdapter);
                loadFinishView(viewPager, mAdapter);
            }
        }
    }

    public FragmentStatePagerAdapter getmAdapter() {
        return mAdapter;
    }

    public abstract void addSubViewTab();

    public abstract void loadFinishView(ViewPager viewPager, FragmentStatePagerAdapter mAdapter);

    public void addTab(String tag, Class<? extends Fragment> fragment){
        fragList.add(new ViewPageInfo(tag, Fragment.instantiate(getActivity(), fragment.getName())));
    }

    /**
     * ViewPageInformation
     */
    public static class ViewPageInfo {
        public String tag;
        public View view;
        public Fragment fragment;

        public ViewPageInfo(String tag, Fragment fragment){
            this.tag = tag;
            this.fragment = fragment;
        }
    }
}
