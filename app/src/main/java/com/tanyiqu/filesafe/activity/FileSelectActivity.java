package com.tanyiqu.filesafe.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.fragment.FilesFragment;
import com.tanyiqu.filesafe.utils.ToastUtil;
import com.tanyiqu.filesafe.utils.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
            final FileInfo item = adapterFiles.get(position);
            holder.img_files_logo.setImageDrawable(getDrawable(item.imgID));
            holder.tv_file_name.setText(item.name);
            holder.tv_file_size.setText(item.size);
            holder.tv_file_date.setText(item.date);
            //点击事件
            holder.files_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(item.size.equals("文件夹")){
//                        Toast.makeText(FileSelectActivity.this, "点击文件夹：" + item.name, Toast.LENGTH_SHORT).show();
                        enterDir(item.parent + File.separator + item.name);
                    }else {
//                        Toast.makeText(FileSelectActivity.this, "点击：" + item.parent+ File.separator + item.name,Toast.LENGTH_SHORT).show();
                        //加密此文件
                        //弹出对话框，确定加密
                        new AlertDialog.Builder(FileSelectActivity.this)
                                .setTitle("加密")
                                .setMessage(item.name)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if(fileIsExist(item.name)){//如果配置文件列表里面已经有此文件，直接返回
                                            ToastUtil.errorToast(FileSelectActivity.this,"文件已存在！");
                                            return;
                                        }
                                        encrypt(item.parent+ File.separator + item.name);
                                        finish();
                                        //刷新显示
                                        FilesFragment.refreshFileView_list(FilesFragment.path);
                                        FilesFragment.refreshFileView_screen();
                                    }
                                })
                                .setNegativeButton("取消",null)
                                .show();
                    }
                }
            });
            //长按事件
            holder.files_item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(FileSelectActivity.this,"长按：" + item.name, Toast.LENGTH_SHORT).show();
                    return true;
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

    //判断文件在配置文件里是否已经存在
    //隐含条件：FilesFragment.path 为当前进入的目录
    public boolean fileIsExist(String name){
        Toast.makeText(this, FilesFragment.path, Toast.LENGTH_SHORT).show();
        String iniPath = FilesFragment.path + File.separator + "data.db";
        File iniFile = new File(iniPath);
        boolean flag = false;
        //检索是否出现 name
        try {
            FileReader in = new FileReader(iniFile);
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null){
                String[] strings = line.split(Data.Splitter);
                if(strings[0].equals(name)){//如果重复了
                    flag = true;
                    break;
                }
            }
            br.close();
        } catch (IOException e) {
            return true;
        }
        return flag;
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
