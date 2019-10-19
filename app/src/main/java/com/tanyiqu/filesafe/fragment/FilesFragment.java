package com.tanyiqu.filesafe.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.activity.FileSelectActivity;
import com.tanyiqu.filesafe.activity.MainActivity;
import com.tanyiqu.filesafe.adapter.FilesAdapter;
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.exception.NoSuchFileToPlayException;
import com.tanyiqu.filesafe.utils.ToastUtil;
import com.tanyiqu.filesafe.utils.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FilesFragment extends Fragment {

    //当前所在的目录完整路径
    public static String path;
    //当前所在的目录的名字
    public static String name;

    public static RecyclerView recycler;

    public FilesFragment(String path,String name){
        super();
        FilesFragment.path = path;
        FilesFragment.name = name;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_files_plus,container,false);
        initToolBar(root);
        initRecycler(root);
        addListeners(root);
        return root;
    }

    private void addListeners(final View root) {

        //悬浮按钮
        FloatingActionButton fab_add = root.findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //添加文件
                //调用文件选择器，选择文件进行加密
                Intent intent = new Intent(getActivity(),FileSelectActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initToolBar(View root) {
        final Toolbar toolbar = root.findViewById(R.id.toolbar_files);
        toolbar.setTitle(name);
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

    private void initRecycler(View root) {
        recycler = root.findViewById(R.id.recycler_files);

        LinearLayoutManager layoutManager = new LinearLayoutManager(root.getContext(),RecyclerView.VERTICAL,false);
        recycler.setLayoutManager(layoutManager);

        FilesAdapter adapter = new FilesAdapter(Data.fileViewList);
        recycler.setAdapter(adapter);
        recycler.setLayoutAnimation(MainActivity.controller);
    }

    //根据 当前FileView的路径 刷新filesView
    //根据 data.db 配置 Data.fileViewList
    public static void refreshFileView(String path){
        if(Data.fileViewList == null){
            Data.fileViewList = new ArrayList<FilesAdapter.FileView>();
        }else {
            Data.fileViewList.clear();
        }
        String iniPath = path + File.separator + "data.db";
        File iniFile = new File(iniPath);
        try {
            FileReader in = new FileReader(iniFile);
            BufferedReader br = new BufferedReader(in);
            String line = null;
            while((line = br.readLine()) != null){
                String[] strings = line.split(Data.Splitter);
                FilesAdapter.FileView fv = new FilesAdapter.FileView(strings[0],strings[1],strings[2],strings[3],path);
                Data.fileViewList.add(fv);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
