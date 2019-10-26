package com.tanyiqu.filesafe.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.tanyiqu.filesafe.utils.FileUtil;
import com.tanyiqu.filesafe.utils.ScreenSizeUtil;
import com.tanyiqu.filesafe.utils.ToastUtil;
import com.tanyiqu.filesafe.utils.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    static Handler handler_single;
    static Handler handler_muti;

    final int MSG_COPY_RUNNING_S = 0x01;
    final int MSG_COPY_FINISH_S = 0x02;

    final int MSG_COPY_START_M = 0x03;
    final int MSG_COPY_RUNNING_M = 0x04;
    final int MSG_COPY_FINISH_M = 0x05;

    //进度
    int prog = 0;
    //复制文件夹时的当前个数
    int curr = 1;
    //复制文件夹时的总数
    int total;

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
                fileInfo.imgID = R.mipmap.ic_file_folder;
                //计算出子文件数量，不加隐藏文件
                int count = 0;
                File[] sub = f.listFiles();
                assert sub != null;
                for(File subFile : sub){
                    if(subFile.isHidden()){
                        continue;
                    }
                    count++;
                }
                fileInfo.size = count + "项";
                Dirs.add(fileInfo);
            }else { //文件
                fileInfo.imgID = FileUtil.getImgId(FileUtil.getFileExt(fileInfo.name));//根据扩展名，适配图标
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
        toolbar.setSubtitle("长按选择文件夹");
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
            FilesFragment.refreshFileView_list(FilesFragment.path);
            FilesFragment.refreshFileView_screen();
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
                    if(item.size.contains("项")){
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
                                        encrypt_single(item.parent+ File.separator + item.name);
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
//        Toast.makeText(this, FilesFragment.path, Toast.LENGTH_SHORT).show();
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
    public void encrypt_single(String enPath){
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
        //两个文件已经准备好
        //复制文件到目标目录
        // orFile -> enFile
        copyFileDialog(FileSelectActivity.this,orFile,enFile);
        //配置文件更新
        Util.updateIni(new File(FilesFragment.path,"data.db"),orFile.getName(),enFile.getName(),orFile.lastModified(),orFile.length());
    }

    public boolean copyFileDialog(final Context context, final File oldFile, final File newFile) {
        //检查参数
        if (!oldFile.exists()) {
            Toast.makeText(this, "失败", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!oldFile.isFile()) {
            Toast.makeText(this, "失败", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!oldFile.canRead()) {
            Toast.makeText(this, "失败", Toast.LENGTH_SHORT).show();
            return false;
        }
        //弹出一个对话框
        final Dialog dialog = new Dialog(context, R.style.NormalDialogStyle);
        View v = View.inflate(context, R.layout.layout_copy_file_dialog, null);
        final ProgressBar bar = v.findViewById(R.id.progress_bar);
        final TextView conform = v.findViewById(R.id.conform);
        final TextView progress = v.findViewById(R.id.progress);
        final TextView title = v.findViewById(R.id.title);
        title.setText("正在复制..");
        conform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                handler_single = null;
//                finish();
//                //刷新显示
//                FilesFragment.refreshFileView_list(FilesFragment.path);
//                FilesFragment.refreshFileView_screen();
            }
        });
        if(handler_single == null){
            handler_single = new Handler(){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    progress.setText(prog+"%");
                    bar.setProgress(prog);
                    if (msg.what == MSG_COPY_FINISH_S) {
//                        Toast.makeText(context, "完成", Toast.LENGTH_SHORT).show();
                        title.setText("复制完成");
                        conform.setVisibility(View.VISIBLE);
                    }
                }
            };
        }
        //开启线程复制文件
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                prog = 0;
                long m = oldFile.length();
                long n = m/100;//一份的大小
                long s = 0;
                try {
                    FileInputStream fis = new FileInputStream(oldFile);
                    FileOutputStream fos = new FileOutputStream(newFile);
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while(-1 != (byteRead = fis.read(buffer))){
                        fos.write(buffer,0,byteRead);
                        s += 1024;
                        if(s >= n){
                            handler_single.sendEmptyMessage(MSG_COPY_RUNNING_S);
                            prog++;
                            s = 0;
                        }
                    }
                    fis.close();
                    fos.flush();
                    fos.close();
                    prog = 100;
                    handler_single.sendEmptyMessage(MSG_COPY_FINISH_S);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // 设置自定义的布局
        dialog.setContentView(v);
        //使得点击对话框外部不消失对话框
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        //设置对话框的大小
        v.setMinimumHeight((int) (ScreenSizeUtil.getScreenHeight(context) * 0.15f));
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp;
        if (dialogWindow != null) {
            lp = dialogWindow.getAttributes();
            if (lp != null) {
                lp.width = (int) (ScreenSizeUtil.getScreenWidth(context) * 0.88);
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;
            }
            dialogWindow.setAttributes(lp);
        }
        dialog.show();
        thread.start();
        return true;
    }

}
