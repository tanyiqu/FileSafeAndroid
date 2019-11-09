package com.tanyiqu.filesafe.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tanyiqu.filesafe.bean.FileSelectBean;
import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.utils.FileUtil;
import com.tanyiqu.filesafe.utils.ScreenSizeUtil;
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

public class FileSelectActivity extends AppCompatActivity {

    RecyclerView recycler;
    LinearLayoutManager layoutManager;
    Toolbar toolbar;
    String currPath = Data.externalStoragePath;
    String parentPath = null;
    //上边显示路径
    TextView path;
    //adapter用于找到哪些是被选中的
    FileAdapter adapter;
    //记录当前在第几层
    int floor = 1;
    //记录所有层的第一个view的位置和偏移
    int[] positions = new int[100];
    int[] offset = new int[100];
    //记录全选时当前的位置和偏移
    int pos;
    int off;

    static Handler handler;

    final int MSG_COPY_START = 0x12;
    final int MSG_COPY_RUNNING = 0x34;
    final int MSG_COPY_FINISH = 0x56;

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

    /**
     * 初始化
     */
    private void init() {

        initToolBar();

        initRecycler();
        
        path = findViewById(R.id.tv_path);

        //默认先进入根目录
        enterDir(currPath,false);
        currPath = Data.externalStoragePath;
        parentPath = null;
    }

    /**
     * 进入目录
     * @param dirPath 目录路径
     * @param isBack 是否为返回
     */
    private void enterDir(String dirPath,boolean  isBack) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        assert files != null;
        //遍历文件夹
        List<FileSelectBean> Dirs = new ArrayList<>();
        List<FileSelectBean> Files = new ArrayList<>();
        for(File f : files){
            FileSelectBean item = new FileSelectBean();
            if(f.isHidden()){
                continue;
            }
            item.setParent(f.getParent());
            item.setName(f.getName());
            item.setDate(Util.transferLongToDate(f.lastModified()));
            if(f.isDirectory()){//文件夹
                item.setImgID(R.mipmap.ic_file_folder);
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
                item.setSize(count + "项");
                item.setDir(true);
                Dirs.add(item);
            }else { //文件
                item.setImgID(FileUtil.getImgId(FileUtil.getFileExt(item.getName())));//根据扩展名，适配图标
                item.setSize(Util.byteToSize(f.length()));
                Files.add(item);
            }
        }
        //排序 先按照中文升序，在按字母升序
        Comparator<FileSelectBean> comparator = new Comparator<FileSelectBean>() {
            @Override
            public int compare(FileSelectBean l, FileSelectBean r) {
                return Collator.getInstance(Locale.CHINESE).compare(l.getName(), r.getName());
            }
        };
        Collections.sort(Dirs,comparator);
        Collections.sort(Files,comparator);
        //合并
        Dirs.addAll(Files);
        //显示
        adapter = new FileAdapter(Dirs);
        recycler.setAdapter(adapter);
        adapter.syncCount();

        //是返回操作就回到上次位置
        if(isBack){
            layoutManager.scrollToPositionWithOffset(positions[floor],offset[floor]);
        }else {//不是返回操作才设置动画效果
            recycler.setLayoutAnimation(DirsActivity.controller);
        }
        //更新路径
        currPath = dirPath;
        if(currPath.equals(Data.externalStoragePath)){
            parentPath = null;
        }else {
            parentPath = dir.getParent();
        }
        //显示路径
        path.setText(currPath.replace(Data.externalStoragePath,"内部存储设备"));
    }

    /**
     * 初始化Recycler
     */
    private void initRecycler() {
        recycler = findViewById(R.id.recycler_file_select);
        layoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        recycler.setLayoutManager(layoutManager);

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                Log.i("MyApp","滑动");
                //当前为第floor层
                //记录偏移
                View firstView = layoutManager.getChildAt(0);
                if(firstView != null){
                    offset[floor] = firstView.getTop();
                    positions[floor] = layoutManager.getPosition(firstView);
                }
            }
        });
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar = findViewById(R.id.toolbar_file_select);
        toolbar.setTitle("选择文件");
        toolbar.setSubtitle("0/29");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //添加菜单按钮
        toolbar.inflateMenu(R.menu.menu_select_file_toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    //反选
                    case R.id.action_inverse_select:
                        //反选文件
                        adapter.inverseSelect();
                        //转到指定位置
                        layoutManager.scrollToPositionWithOffset(pos,off);
                        break;
                    //全选
                    case R.id.action_select_all:
                        //选择全部文件
                        adapter.selectAll();
                        //转到指定位置
                        layoutManager.scrollToPositionWithOffset(pos,off);
                        break;
                    //添加
                    case R.id.action_add_files:
                        return addFiles();
                }
                return false;
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 添加文件
     * @return 返回false，详见调用处
     */
    private boolean addFiles(){
        //获取已选择的文件列表
        final List<String> paths = adapter.getSelected();

        //如果没有选择文件，直接返回
        if(paths.size() == 0){
            Toast.makeText(FileSelectActivity.this, "请选择文件", Toast.LENGTH_SHORT).show();
            return false;
        }
        //依次判断重复的文件
        List<String> repeat = new ArrayList<>();
        for(String path : paths){
            //截取名字
            String name = path.substring(path.lastIndexOf(File.separator) + 1);
            //判断名字是否重复
            if(fileIsExist(name)){
                repeat.add(name);
            }
        }
        //repeat里面的全部是重复的文件
        if(repeat.size() != 0){//有重复的文件
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            sb.append(repeat.get(0));
            sb.append("\"\n");
            if(repeat.size() >= 2){
                sb.append("\"");
                sb.append(repeat.get(1));
                sb.append("\"...\n");
            }
            sb.append("等");
            sb.append(repeat.size());
            sb.append("个文件已存在");
            new AlertDialog.Builder(FileSelectActivity.this)
                    .setTitle("警告")
                    .setMessage(sb)
                    .setPositiveButton("确定",null)
                    .show();
            return false;
        }
        //现在paths里已经没有重复的文件了
        //弹出对话框确认加密
        new AlertDialog.Builder(FileSelectActivity.this)
                .setMessage("确定加密选择的" + paths.size() + "个文件")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //加密所选的文件
                        //构造加密文件列表
                        List<File> oldFiles = new ArrayList<>();
                        List<File> newFiles = new ArrayList<>();
                        for(String path : paths){
                            //构造old
                            File oldFile = new File(path);
                            //构造new
                            //随机一个不重复的文件名
                            File newFile;
                            String newFileName;
                            do {
                                newFileName = Util.RandomName();
                                newFile = new File(FilesActivity.path, newFileName);
                                //如果文件已存在，重新随机一个
                            } while (newFile.exists());
                            oldFiles.add(oldFile);
                            newFiles.add(newFile);
                            //配置文件更新
                            Util.updateIni(new File(FilesActivity.path,"data.db"),
                                    oldFile.getName(),
                                    newFile.getName(),
                                    oldFile.lastModified(),
                                    oldFile.length());
                        }
                        copyDirDialog(FileSelectActivity.this,oldFiles,newFiles);
                    }
                })
                .setNegativeButton("取消",null)
                .show();
        return false;
    }

    /**
     * 重写返回事件
     */
    @Override
    public void onBackPressed() {
        //如果没有上一级，默认返回
        if(currPath.equals(Data.externalStoragePath)){
            super.onBackPressed();
            overridePendingTransition(R.anim.anim_page_jump_3,R.anim.anim_page_jump_4);
            FilesActivity.refreshFileView_list(FilesActivity.path);
        }else{
            //返回上一级
            floor--;
            enterDir(parentPath,true);
        }
    }

    /**
     * 重写onCreateOptionsMenu
     * @param menu menu
     * @return true or false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_file_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 判断文件在配置文件里是否已经存在
     * 隐含条件：FilesActivity.path 为当前进入的目录
     * @param name 文件名
     * @return 是否已经存在
     */
    public boolean fileIsExist(String name){
        String iniPath = FilesActivity.path + File.separator + "data.db";
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

    /**
     * 显示复制文件的对话框兼复制文件功能
     * Handler警告未消除。。
     * @param context Context
     * @param oldFiles 原始文件列表
     * @param newFiles 新文件列表
     */
    public void copyDirDialog(final Context context, final List<File> oldFiles, final List<File> newFiles){
        if(oldFiles.size() == 0){
            Toast.makeText(context, "空", Toast.LENGTH_SHORT).show();
            return;
        }
        final Dialog dialog = new Dialog(context, R.style.NormalDialogStyle);
        View v = View.inflate(context, R.layout.layout_dialog_copy, null);
        final ProgressBar bar = v.findViewById(R.id.progress_bar);
        final TextView title  = v.findViewById(R.id.title);
        final TextView tv_msg = v.findViewById(R.id.tv_msg);
        final TextView progress = v.findViewById(R.id.progress);
        final TextView confirm = v.findViewById(R.id.conform);
        title.setText("正在复制");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                handler = null;
                finish();
                overridePendingTransition(R.anim.anim_page_jump_3,R.anim.anim_page_jump_4);
                //刷新显示
                FilesActivity.refreshFileView_list(FilesActivity.path);
            }
        });
        //Handler
        if(handler == null){
            handler = new Handler(){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what){
                        case MSG_COPY_START:
                            String t1 = "第"+curr+"/"+total+"个";
                            tv_msg.setText(t1);
                            break;
                        case MSG_COPY_FINISH:
                            title.setText("复制完成");
                            tv_msg.setText("");
                            confirm.setVisibility(View.VISIBLE);
                        case MSG_COPY_RUNNING:
                            bar.setProgress(prog);
                            String t2 = prog+"%";
                            progress.setText(t2);
                            break;
                    }
                }
            };
        }
        //Thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //循环复制文件
                curr = 1;
                for (int i=0;i<total;i++){
                    //提示正在开始复制第几个
                    handler.sendEmptyMessage(MSG_COPY_START);
                    prog = 0;
                    try {
                        long m = oldFiles.get(i).length();
                        int n = (int)(m/100);
                        int s = 0;//每一份的进度
                        FileInputStream fis = new FileInputStream(oldFiles.get(i));
                        FileOutputStream fos = new FileOutputStream(newFiles.get(i));
                        byte[] buffer = new byte[1024];
                        int byteRead;
                        while (-1 != (byteRead = fis.read(buffer))){
                            fos.write(buffer,0,byteRead);
                            s += 1024;
                            if(s >= n){//进度+1
                                handler.sendEmptyMessage(MSG_COPY_RUNNING);
                                prog ++;
                                s = 0;
                            }
                        }
                        fis.close();
                        fos.flush();
                        fos.close();
                        //复制完成1个
                        if(curr < total){
                            curr++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                prog = 100;
                handler.sendEmptyMessage(MSG_COPY_FINISH);
            }
        });

        total = oldFiles.size();

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
    }

    /**
     * Adapter
     * 内部类
     */
    class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder>{

        private List<FileSelectBean> fileList;

        private SparseBooleanArray checkStatus;

        /**
         * 构造
         * @param fileList File列表
         */
        FileAdapter(List<FileSelectBean> fileList) {
            this.fileList = fileList;
            checkStatus = new SparseBooleanArray();
            //默认都未选中
            for(int i=0;i<fileList.size();i++){
                checkStatus.put(i,false);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_files_item_select,parent,false);
            return new ViewHolder(root);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            final FileSelectBean item = fileList.get(position);
            holder.img_files_logo.setImageDrawable(getDrawable(item.getImgID()));
            holder.tv_file_name.setText(item.getName());
            holder.tv_file_size.setText(item.getSize());
            holder.tv_file_date.setText(item.getDate());
            holder.checkBox.setOnCheckedChangeListener(null);

            holder.checkBox.setVisibility(View.VISIBLE);
            if(checkStatus.get(position)){
                holder.checkBox.setChecked(true);
            }else {
                holder.checkBox.setChecked(false);
            }
            if(item.isDir()){
                holder.checkBox.setVisibility(View.GONE);
            }
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        checkStatus.put(position,true);
                    }else {
                        checkStatus.put(position,false);
                    }
                }
            });
            //点击事件
            holder.files_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(item.isDir()){
                        floor++;
                        enterDir(item.getParent() + File.separator + item.getName(),false);
                    }else {
                        //改变选中的状态
                        if(holder.checkBox.isChecked()){
                            holder.checkBox.setChecked(false);
                        }else{
                            holder.checkBox.setChecked(true);
                        }
                        syncCount();
                    }
                }
            });
            //长按事件
            holder.files_item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(FileSelectActivity.this,"长按：" + item.getName(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.fileList.size();
        }

        /**
         * 返回被选中的项
         * @return 字符串列表形式返回
         */
        List<String> getSelected(){
            List<String> strings = new ArrayList<>();
            for(int i=0;i<fileList.size();i++){
                if(checkStatus.get(i)){
                    strings.add(fileList.get(i).getParent() + File.separator + fileList.get(i).getName());
                }
            }
            return strings;
        }

        /**
         * 同步选中的数目
         * 在进入文件夹时、全选/反选时、点击文件时调用
         */
        void syncCount(){
            //获取总共可选的文件数
            int totalCount = 0;
            for(FileSelectBean item : fileList){
                if(item.isDir()){
                    continue;
                }
                totalCount++;
            }
            int currCount = getSelected().size();
            String s = currCount + "/" + totalCount;
            toolbar.setSubtitle(s);
        }

        /**
         * 全选
         */
        void selectAll(){
            //先将所有状态设为true
            for(int i=0;i<fileList.size();i++){
                FileSelectBean bean = fileList.get(i);
                if(bean.isDir())
                    continue;
                checkStatus.put(i,true);
            }
            //记录位置和偏移
            View firstView = layoutManager.getChildAt(0);
            if(firstView != null){
                off = firstView.getTop();
                pos = layoutManager.getPosition(firstView);
            }
            //刷新
            recycler.setAdapter(this);
            syncCount();
        }

        /**
         * 反选
         */
        void inverseSelect(){
            for(int i=0;i<fileList.size();i++){
                FileSelectBean bean = fileList.get(i);
                if(bean.isDir())
                    continue;
                if(checkStatus.get(i)){
                    checkStatus.put(i,false);
                }else {
                    checkStatus.put(i,true);
                }
            }
            //记录位置和偏移
            View firstView = layoutManager.getChildAt(0);
            if(firstView != null){
                off = firstView.getTop();
                pos = layoutManager.getPosition(firstView);
            }
            //刷新
            recycler.setAdapter(this);
            syncCount();
        }

        /**
         * Holder
         */
        class ViewHolder extends RecyclerView.ViewHolder{

            ConstraintLayout files_item;
            ImageView img_files_logo;
            TextView tv_file_name;
            TextView tv_file_size;
            TextView tv_file_date;
            CheckBox checkBox;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                files_item = itemView.findViewById(R.id.files_item);
                img_files_logo = itemView.findViewById(R.id.img_files_logo);
                tv_file_name = itemView.findViewById(R.id.tv_file_name);
                tv_file_size = itemView.findViewById(R.id.tv_file_size);
                tv_file_date = itemView.findViewById(R.id.tv_file_date);
                checkBox = itemView.findViewById(R.id.check);
            }
        }
    }

}
