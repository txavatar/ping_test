package com.example.shi.nettest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shi.nettest.R;

public class FragmentSpeed extends Fragment {
    private View rootView;//缓存Fragment view

    public FragmentSpeed() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if(rootView==null){
            rootView=inflater.inflate(R.layout.activity_tab_speed, container, false);
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        TextView tv = (TextView)rootView.findViewById(R.id.speed_tv);

        tv.setText("XXXX");
        return rootView;
    }
}
