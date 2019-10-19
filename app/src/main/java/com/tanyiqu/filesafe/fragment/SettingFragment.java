package com.tanyiqu.filesafe.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.tanyiqu.filesafe.R;

public class SettingFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting,container,false);

        //初始化ToolBar
        initToolBar(root);


        return root;
    }

    private void initToolBar(View root) {
        final Toolbar toolbar = root.findViewById(R.id.toolbar_setting);
        toolbar.setTitle("设置");
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
}
