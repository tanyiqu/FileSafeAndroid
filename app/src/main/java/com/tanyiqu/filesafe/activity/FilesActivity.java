package com.tanyiqu.filesafe.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tanyiqu.filesafe.R;
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

public class FilesActivity extends AppCompatActivity {

    public static String path;
    String name;
    static RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        Bundle extra = getIntent().getExtras();
        path = extra.getString("path");
        name = extra.getString("name");

        init();



    }

    private void init() {
        //设置共享元素的图片
        ImageView img = findViewById(R.id.img_cover);
        Bitmap bmp= BitmapFactory.decodeFile(path + File.separator + "cover.jpg");
        img.setImageBitmap(bmp);

        initToolBar();

        initRecycler();

        addListeners();
    }

    private void initToolBar() {
        final Toolbar toolbar = findViewById(R.id.toolbar_files);
        setSupportActionBar(toolbar);
        toolbar.setTitle(name);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initRecycler() {
        recycler = findViewById(R.id.recycler_files);
        LinearLayoutManager layoutManager = new LinearLayoutManager(FilesActivity.this,RecyclerView.VERTICAL,false);
        recycler.setLayoutManager(layoutManager);
        FilesAdapter adapter = new FilesAdapter(Data.fileViewList);
        recycler.setAdapter(adapter);
        //动画效果;
    }

    private void addListeners() {
        //悬浮按钮
        FloatingActionButton fab_add = findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //添加文件
                //调用文件选择器，选择文件进行加密
                Intent intent = new Intent(view.getContext(),FileSelectActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_page_jump_1,R.anim.anim_page_jump_2);
            }
        });
    }

    //刷新列表
    public static void refreshFileView_list(String path){
        List<FileView> list = new ArrayList<FileView>();;
        String iniPath = path + File.separator + "data.db";
        File iniFile = new File(iniPath);
        try {
            FileReader in = new FileReader(iniFile);
            BufferedReader br = new BufferedReader(in);
            String line = null;
            while((line = br.readLine()) != null){
                String[] strings = line.split(Data.Splitter);
                FileView fv = new FileView(strings[0],strings[1],strings[2],strings[3],path);
                list.add(fv);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Data.setFileViewList(list);
    }
    private void refreshFileView_screen(){
        FilesAdapter adapter = new FilesAdapter(Data.fileViewList);
        recycler.setAdapter(adapter);
//        recycler.setLayoutAnimation(DirsActivity.controller);
    }

    /**
     * Adapter类
     */
    public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder>{

        private List<FileView> fileViewList;

        public FilesAdapter(List<FileView> fileViewList) {
            this.fileViewList = fileViewList;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            ConstraintLayout files_item;
            ImageView img_files_logo;
            TextView tv_file_name;
            TextView tv_file_size;
            TextView tv_file_date;

            String original_name;    //原文件名字
            String encrypted_name;   //加密后的名字
            public String path;             //file所在的目录路径

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                files_item = itemView.findViewById(R.id.files_item);
                img_files_logo = itemView.findViewById(R.id.img_files_logo);
                tv_file_name = itemView.findViewById(R.id.tv_file_name);
                tv_file_size = itemView.findViewById(R.id.tv_file_size);
                tv_file_date = itemView.findViewById(R.id.tv_file_date);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_files_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final FileView item = fileViewList.get(position);
            final String ext = FileUtil.getFileExt(item.original_name);

            holder.img_files_logo.setImageDrawable(getDrawable(FileUtil.getImgId(ext)));
            holder.tv_file_name.setText(item.original_name);
            holder.tv_file_size.setText(item.size);
            holder.tv_file_date.setText(item.date);
            holder.original_name = item.original_name;
            holder.encrypted_name = item.encrypted_name;
            //设置点击事件
            holder.files_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //打开此文件
                    try {
                        File file = new File(item.path + File.separator + item.encrypted_name);
                        FileUtil.openFile(view.getContext(),file ,ext);
                    } catch (NoSuchFileToOpenException e) {
                        ToastUtil.errorToast(view.getContext(),e.getMessage());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return fileViewList.size();
        }

    }

    public static class FileView {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFileView_list(path);
        refreshFileView_screen();
    }
}
