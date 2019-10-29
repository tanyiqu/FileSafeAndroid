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
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.exception.NoSuchFileToOpenException;
import com.tanyiqu.filesafe.utils.FileUtil;
import com.tanyiqu.filesafe.utils.ToastUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        View root = inflater.inflate(R.layout.fragment_files,container,false);
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
                getActivity().overridePendingTransition(R.anim.anim_page_jump_1,R.anim.anim_page_jump_2);
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
    //刷新 List
    public static void refreshFileView_list(String path){
        if(Data.fileViewList == null){
            Data.fileViewList = new ArrayList<FilesFragment.FileView>();
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
                FilesFragment.FileView fv = new FilesFragment.FileView(strings[0],strings[1],strings[2],strings[3],path);
                Data.fileViewList.add(fv);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //刷新 显示
    //刷新 屏幕
    public static void refreshFileView_screen(){
        FilesFragment.FilesAdapter adapter = new FilesAdapter(Data.fileViewList);
        recycler.setAdapter(adapter);
        recycler.setLayoutAnimation(MainActivity.controller);
    }


    /**
     * 适配器类
     */
    public static class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

        private List<FileView> fileViewList;

        //ViewHolder类
        public class ViewHolder extends RecyclerView.ViewHolder {

            //主视图 用于点击/长按事件
            ConstraintLayout files_item;
            ImageView img_files_logo;
            TextView tv_file_name;
            TextView tv_file_size;
            TextView tv_file_date;

            String original_name;    //原文件名字
            String encrypted_name;   //加密后的名字
            public String path;             //file所在的目录路径

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                files_item = itemView.findViewById(R.id.files_item);
                img_files_logo = itemView.findViewById(R.id.img_files_logo);
                tv_file_name = itemView.findViewById(R.id.tv_file_name);
                tv_file_size = itemView.findViewById(R.id.tv_file_size);
                tv_file_date = itemView.findViewById(R.id.tv_file_date);
            }
        }

        //构造
        public FilesAdapter(List<FilesFragment.FileView> fileViewList){
            this.fileViewList = fileViewList;
        }

        @NonNull
        @Override
        public FilesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_files_item,parent,false);
            return new ViewHolder(root);

        }

        @Override
        public void onBindViewHolder(@NonNull FilesAdapter.ViewHolder holder, int position) {
            final FilesFragment.FileView fileView = fileViewList.get(position);
            final String ext = FileUtil.getFileExt(fileView.original_name);;
            holder.files_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //打开此文件
                    try {
                        File file = new File(fileView.path + File.separator + fileView.encrypted_name);
                        FileUtil.openFile(view.getContext(),file ,ext);
                    } catch (NoSuchFileToOpenException e) {
                        ToastUtil.errorToast(view.getContext(),e.getMessage());
                    }
                }
            });
            holder.img_files_logo.setImageDrawable(MainActivity.activity.getDrawable(FileUtil.getImgId(ext)));
            holder.tv_file_name.setText(fileView.original_name);
            holder.tv_file_size.setText(fileView.size);
            holder.tv_file_date.setText(fileView.date);
            holder.original_name = fileView.original_name;
            holder.encrypted_name = fileView.encrypted_name;
            holder.path = fileView.path;
        }

        @Override
        public int getItemCount() {
            return fileViewList.size();
        }

    }

    public static class FileView {
        //封面
        public Drawable drawable;
        public int imgID;
        public String coverPath;
        //名字
        public String original_name;    //原文件名字
        public String encrypted_name;   //加密后的名字
        //日期
        public String date;
        //大小
        public String size;
        //file 所在的路径
        public String path;

        //原文件名 加密后文件名 日期 大小 所在路径（用于打开文件）
        public FileView(String original_name, String encrypted_name, String date, String size, String path) {
            this.original_name = original_name;
            this.encrypted_name = encrypted_name;
            this.date = date;
            this.size = size;
            this.path = path;
        }

        public FileView(Drawable drawable,String original_name, String encrypted_name, String date, String size, String path) {
            this.original_name = original_name;
            this.encrypted_name = encrypted_name;
            this.date = date;
            this.size = size;
            this.path = path;
        }

    }

}
