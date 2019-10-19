package com.tanyiqu.filesafe.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.adapter.FilesAdapter;
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.fragment.FilesFragment;
import com.tanyiqu.filesafe.utils.Util;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FileSelectActivity extends Activity {

    RecyclerView recycler;
    String currPath = Data.externalStoragePath;
    String parentPath = null;
    TextView tv_path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);

        init();
    }

    private void init() {

        initToolBar();

        initRecycler();
        
        tv_path = findViewById(R.id.tv_path);

        //默认先进入根目录
        enterDir(currPath);
        currPath = Data.externalStoragePath;
        parentPath = null;
    }

    private void enterDir(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        assert files != null;
        //遍历文件夹
        List<FileInfo> Dirs = new ArrayList<FileInfo>();
        List<FileInfo> Files = new ArrayList<FileInfo>();
        for(File f : files){
            FileInfo fileInfo = new FileInfo();
            if(f.isHidden()){
                continue;
            }
            fileInfo.parent = f.getParent();
            fileInfo.name = f.getName();
            fileInfo.date = Util.transferLongToDate(f.lastModified());
            if(f.isDirectory()){//文件夹
                fileInfo.imgID = R.mipmap.ic_launcher_rem;
                fileInfo.size = "文件夹";
                Dirs.add(fileInfo);
            }else { //文件
                fileInfo.imgID = R.mipmap.doc;
                fileInfo.size = Util.byteToSize(f.length());
                Files.add(fileInfo);
            }
        }
        //排序 先按照中文升序，在按字母升序
        Comparator<FileInfo> comparator = new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo l, FileInfo r) {
                return Collator.getInstance(Locale.CHINESE).compare(l.name,r.name);
            }
        };
        Collections.sort(Dirs,comparator);
        Collections.sort(Files,comparator);
        //合并
        Dirs.addAll(Files);
        //显示
        FileAdapter adapter = new FileAdapter(Dirs);
        recycler.setAdapter(adapter);
        recycler.setLayoutAnimation(MainActivity.controller);
        //更新路径
        currPath = dirPath;
        if(currPath.equals(Data.externalStoragePath)){
            parentPath = null;
        }else {
            parentPath = dir.getParent();
        }
        //显示路径
        tv_path.setText(currPath.replace(Data.externalStoragePath,"内部存储设备"));

    }

    private void initRecycler() {
        recycler = findViewById(R.id.recycler_file_select);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        recycler.setLayoutManager(layoutManager);
    }

    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_file_select);
        toolbar.setTitle("选择文件");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //如果没有上一级，默认返回
        if(currPath.equals(Data.externalStoragePath)){
            super.onBackPressed();
        }else{
            //返回上一级
            enterDir(parentPath);
        }
    }

    /**
     * Adapter
     */
    class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder>{

        private List<FileInfo> adapterFiles;

        FileAdapter(List<FileInfo> adapterFiles) {
            this.adapterFiles = adapterFiles;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_files_item_select,parent,false);
            return new ViewHolder(root);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final FileInfo fileInfo = adapterFiles.get(position);
            holder.img_files_logo.setImageDrawable(getDrawable(fileInfo.imgID));
            holder.tv_file_name.setText(fileInfo.name);
            holder.tv_file_size.setText(fileInfo.size);
            holder.tv_file_date.setText(fileInfo.date);
            holder.files_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(fileInfo.size.equals("文件夹")){
//                        Toast.makeText(FileSelectActivity.this, "点击文件夹：" + fileInfo.name, Toast.LENGTH_SHORT).show();
                        enterDir(fileInfo.parent + File.separator + fileInfo.name);
                    }else {
//                        Toast.makeText(FileSelectActivity.this, "点击：" + fileInfo.parent+ File.separator + fileInfo.name,Toast.LENGTH_SHORT).show();
                        //加密此文件
                        //弹出对话框，确定加密
                        new AlertDialog.Builder(FileSelectActivity.this)
                                .setTitle("加密")
                                .setMessage(fileInfo.name)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        encrypt(fileInfo.parent+ File.separator + fileInfo.name);
                                        finish();
                                        //刷新显示
                                        FilesFragment.refreshFileView(FilesFragment.path);
                                        FilesAdapter adapter = new FilesAdapter(Data.fileViewList);
                                        FilesFragment.recycler.setAdapter(adapter);
                                        FilesFragment.recycler.setLayoutAnimation(MainActivity.controller);
                                    }
                                })
                                .setNegativeButton("取消",null)
                                .show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.adapterFiles.size();
        }

        //Holder
        class ViewHolder extends RecyclerView.ViewHolder{

            ConstraintLayout files_item;
            ImageView img_files_logo;
            TextView tv_file_name;
            TextView tv_file_size;
            TextView tv_file_date;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                files_item = itemView.findViewById(R.id.files_item);
                img_files_logo = itemView.findViewById(R.id.img_files_logo);
                tv_file_name = itemView.findViewById(R.id.tv_file_name);
                tv_file_size = itemView.findViewById(R.id.tv_file_size);
                tv_file_date = itemView.findViewById(R.id.tv_file_date);
            }
        }
    }

    class FileInfo{
        int imgID;
        String name;
        String size;
        String date;
        String parent;
    }

    //加密单个文件
    public void encrypt(String enPath){
        File orFile = new File(enPath);
        //随机一个不重复的文件名
        File enFile = null;
        String enName = null;
        while(true){
            enName = Util.RandomName();
            enFile = new File(FilesFragment.path,enName);
            //文件不存在 则 跳出循环
            if(!enFile.exists()){
                break;
            }
        }
        //复制文件到目标目录
        // orFile -> enFile
        Util.copyFile(orFile.getPath(),enFile.getPath());
        //配置文件更新
        Util.updateIni(new File(FilesFragment.path,"data.db"),orFile.getName(),enFile.getName(),orFile.lastModified(),orFile.length());
    }

}
