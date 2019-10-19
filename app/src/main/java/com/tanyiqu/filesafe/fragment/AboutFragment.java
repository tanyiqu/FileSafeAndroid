package com.tanyiqu.filesafe.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.utils.Util;

public class AboutFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about,container,false);
        //初始化Toolbar
        initToolBar(root);
        //给TextView设置随机颜色
        randomColor(root);
        return root;
    }

    private void initToolBar(View root) {
        final Toolbar toolbar = root.findViewById(R.id.toolbar_about);
        toolbar.setTitle("关于");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentActivity activity = getActivity();
                if (activity != null){
                    activity.onBackPressed();
                }
            }
        });
    }

    private void randomColor(View root) {
        TextView[] tvs = new TextView[]{root.findViewById(R.id.tv_aboutApp_content),root.findViewById(R.id.tv_aboutAuthor_content),root.findViewById(R.id.tv_thanks_content)};
        for(TextView tv : tvs){
            int r = Util.RandomInt(1,3);
            switch (r){
                case 1:
                    tv.setBackgroundResource(R.drawable.shape_bg_about_text_01);
                    break;
                case 2:
                    tv.setBackgroundResource(R.drawable.shape_bg_about_text_02);
                    break;
                case 3:
                    tv.setBackgroundResource(R.drawable.shape_bg_about_text_03);
                    break;
                default:
                    tv.setBackgroundResource(R.drawable.shape_bg_about_text_01);
                    break;
            }
        }

    }

}
