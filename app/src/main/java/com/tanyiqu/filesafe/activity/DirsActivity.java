package com.tanyiqu.filesafe.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.utils.FileUtil;
import com.tanyiqu.filesafe.utils.ScreenSizeUtil;
import com.tanyiqu.filesafe.utils.ToastUtil;
import com.tanyiqu.filesafe.utils.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DirsActivity extends AppCompatActivity {

    private static long exitTime = 0;
    public static DrawerLayout drawer;
    public static FragmentManager fragmentManager;
    public static boolean neededDoubleClickToExit = true;
    static RecyclerView recycler;
    static DirsAdapter adapter;
    public static LayoutAnimationController controller;//用作子fragment中的recycler动画
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dirs);
        init();
    }

    private void init() {
        //初始化各种值
        drawer = findViewById(R.id.drawer);

        //初始化ToolBar
        initToolBar();

        fragmentManager = getSupportFragmentManager();

        controller = new LayoutAnimationController(AnimationUtils.loadAnimation(this,R.anim.anim_files_show));

        //初始化侧滑菜单
        initNavigationView();

        initRecycler();

    }

    private void initRecycler() {
        recycler = findViewById(R.id.recycler_dirs);
        GridLayoutManager layoutManager = new GridLayoutManager(DirsActivity.this,2);
        recycler.setLayoutManager(layoutManager);
        adapter = new DirsAdapter(Data.dirViewList);
        recycler.setAdapter(adapter);

    }

    private void initToolBar() {
        final Toolbar toolbar = findViewById(R.id.toolbar_dirs);
        //设置标题
        toolbar.setTitle("保险箱");
        //设置导航Button点击事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DirsActivity.drawer.openDrawer(GravityCompat.START);
            }
        });
        //设置移除更多选项图片  如果不设置会默认使用系统灰色的图标
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_btn_more));
        //添加menu
        toolbar.inflateMenu(R.menu.menu_dirs_toolbar);
        //设置menu点击事件
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_add_dir://新建目录
                        mkDir(toolbar.getContext());
                        break;
                    case R.id.action_setting://设置界面
                        startActivity(new Intent(DirsActivity.this, SettingActivity.class));
                        overridePendingTransition(R.anim.anim_page_jump_1,R.anim.anim_page_jump_2);
                        break;
                    case R.id.action_about://关于界面
                        startActivity(new Intent(DirsActivity.this, AboutActivity.class));
                        overridePendingTransition(R.anim.anim_page_jump_1,R.anim.anim_page_jump_2);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    //初始化侧滑菜单
    private void initNavigationView() {
        NavigationView navigation = findViewById(R.id.navigation);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawer.closeDrawers();
                switch (menuItem.getItemId()){
                    case R.id.action_setting:   //设置页面
                        startActivity(new Intent(DirsActivity.this,SettingActivity.class));
                        overridePendingTransition(R.anim.anim_page_jump_1,R.anim.anim_page_jump_2);
                        break;
                    case R.id.action_about:     //关于页面
                        startActivity(new Intent(DirsActivity.this,AboutActivity.class));
                        overridePendingTransition(R.anim.anim_page_jump_1,R.anim.anim_page_jump_2);
                        break;
                }
                return true;
            }
        });
    }

    //根据配置文件夹 刷新
    public static void refreshDirs_list(){
        List<DirView> dirViewList = new ArrayList<DirView>();
        //列出files文件夹的所有 文件夹
        File dir = new File(Data.externalStoragePath + "/.file_safe/files");
        File[] files = dir.listFiles();
        if (files != null) {
            for(File d : files){//遍历列出的文件夹
                if(d.isDirectory() && !d.isHidden()){
                    //用 封面和名字 构造
                    String name = d.getName();
                    String count = (d.listFiles().length-2) + "";
                    dirViewList.add(new DirView(d.getPath() + File.separator + "cover.jpg",name,count));
                }
            }
        }
        //按名字排序
        Comparator<DirView> comparator = new Comparator<DirView>() {
            @Override
            public int compare(DirView l, DirView r) {
                return Collator.getInstance(Locale.CHINESE).compare(l.name,r.name);
            }
        };
        Collections.sort(dirViewList,comparator);
        Data.setDirViewList(dirViewList);
    }
    public void refreshDirs_screen(){
        adapter = new DirsAdapter(Data.dirViewList);
        recycler.setAdapter(adapter);
    }

    private void mkDir(Context context) {
        final Dialog dialog = Util.inputDialog(context);
        View view = View.inflate(context, R.layout.layout_dialog_input, null);
        TextView cancel = view.findViewById(R.id.dialog_cancel);
        TextView conform = view.findViewById(R.id.dialog_conform);
        TextView title = view.findViewById(R.id.tv_dialog_title);
        title.setText("新建目录（名字别太长>_<）");
        conform.setText("新建");
        final EditText name = view.findViewById(R.id.edit_dir_name);
        //设置对话框的大小
        view.setMinimumHeight((int) (ScreenSizeUtil.getScreenHeight(context) * 0.2f));
        //取消按钮
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        //新建按钮
        conform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取目录名并去掉多余空格
                String dirName = name.getText().toString().trim();
                //如果名字为空，不理他
                if (dirName.equals("")) {
                    return;
                }
                //判断文件夹名是否合法
                if (!Util.checkFileName(dirName)) {
                    Toast.makeText(view.getContext(), "文件名不能包含下列任何字符：\n\\ / : * ? \" < > |\n且首个字符不能为.", Toast.LENGTH_LONG).show();
                    return;
                }
                //构造目录文件
                File newDir = new File(Data.externalStoragePath + File.separator + ".file_safe" + File.separator + "files", dirName);
                //如果已存在，提示错误
                if (newDir.exists()) {
                    ToastUtil.errorToast(view.getContext(), "目录已存在");
                    return;
                }
                //新建文件夹
                if (!newDir.mkdir()) {
                    ToastUtil.errorToast(view.getContext(), "新建目录失败");
                }
                //给新建的文件夹，添加默认封面
                Resources res = getResources();
                BitmapDrawable d;
                //如果输入特定的名字，添加特定的封面
                switch (dirName){
                    case "视频":
                        d = (BitmapDrawable) res.getDrawable(R.mipmap.video);
                        break;
                    case "图片":
                        d = (BitmapDrawable) res.getDrawable(R.mipmap.pic);
                        break;
                    default:
                        d = (BitmapDrawable) res.getDrawable(R.mipmap.other);
                }
                Bitmap img = d.getBitmap();
                String coverPath = newDir.getPath() + File.separator + "cover.jpg";
                String iniPath = newDir.getPath() + File.separator + "data.db";

                try {
                    //创建封面
                    OutputStream os = new FileOutputStream(coverPath);
                    img.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.close();
                    //创建数据文件
                    boolean f = new File(iniPath).createNewFile();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                //刷新显示
                DirsActivity.refreshDirs_list();
                refreshDirs_screen();
                ToastUtil.myToast(view.getContext(), "新建成功！");
            }
        });
        // 设置自定义的布局
        dialog.setContentView(view);
        dialog.show();
        //弹出输入法
        name.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) name.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(name,InputMethodManager.SHOW_IMPLICIT);
                }
            }
        },150);
    }


    /**
     * Adapter
     */
    public class DirsAdapter extends RecyclerView.Adapter<DirsAdapter.ViewHolder>{

        List<DirView> dirViewList;
        private final String[] options = new String[]{"重命名","删除","导出","设置封面"};
        private final static int DEFAULT_SELECT = 0;
        private int currSelectedOption = DEFAULT_SELECT;


        DirsAdapter(List<DirView> dirViewList) {
            this.dirViewList = dirViewList;
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            CardView dirs_item;
            ImageView img_dirs_item;
            TextView tv_dirs_name;
            TextView tv_dirs_count;
            String path;
            //构造
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                dirs_item = itemView.findViewById(R.id.dirs_item);
                img_dirs_item = itemView.findViewById(R.id.img_dirs_item);
                tv_dirs_name = itemView.findViewById(R.id.tv_dirs_name);
                tv_dirs_count = itemView.findViewById(R.id.tv_dirs_count);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_dirs_item_grid,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final DirView item = dirViewList.get(position);
            //添加图片和内容
            Bitmap bmp= BitmapFactory.decodeFile(item.coverPath);
            holder.img_dirs_item.setImageBitmap(bmp);
            //内容
            holder.tv_dirs_name.setText(item.name);
            //数目
            String count = " ("+item.count+"项)";
            holder.tv_dirs_count.setText(count);
            //路径
            holder.path = Data.externalStoragePath + File.separator + ".file_safe" + File.separator  + "files" + File.separator + item.name;
            //设置点击事件
            holder.dirs_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //构造下一个页面的FileViewList
                    FilesActivity.refreshFileView_list(holder.path);
                    //界面跳转，共享元素
                    Intent intent = new Intent(view.getContext(),FilesActivity.class);
                    intent.putExtra("path",holder.path);
                    intent.putExtra("name",item.name);
                    holder.img_dirs_item.setTransitionName("tt");
                    ActivityOptions options  = ActivityOptions.makeSceneTransitionAnimation(DirsActivity.this,holder.img_dirs_item,"tt");
                    DirsActivity.this.startActivity(intent,options.toBundle());
                }
            });
            //设置长按事件
            holder.dirs_item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    currSelectedOption = DEFAULT_SELECT;
                    final Context context = view.getContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(item.name);
                    builder.setSingleChoiceItems(options, DEFAULT_SELECT, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            currSelectedOption = i;
                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //执行操作
                            switch (currSelectedOption){
                                case 0://重命名
                                    renameDirView(context,item.name);
                                    break;
                                case 1://删除
                                    //删除 item.content
                                    rmDirView(context,item.name);
                                    break;
                                case 2://导出

                                    break;
                                case 3://设置封面

                                    break;
                            }
                        }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return dirViewList.size();
        }

        //删除目录
        private void rmDirView(final Context context,final String name){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("删除")
                    .setMessage(name)
                    .setNegativeButton("取消",null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            File dir = new File(Data.filesPath,name);
                            boolean flag = FileUtil.deleteDir_r(dir);
                            refreshDirs_list();
                            refreshDirs_screen();
                            if (flag){
                                ToastUtil.myToast(context,"删除成功");
                            }else {
                                ToastUtil.errorToast(context,"删除失败");
                            }
                        }
                    })
                    .show();
        }

        //重命名目录
        private void renameDirView(final Context context,final String name){
            //弹出对话框
            final Dialog dialog = Util.inputDialog(context);
            View view = View.inflate(context, R.layout.layout_dialog_input, null);
            TextView cancel = view.findViewById(R.id.dialog_cancel);
            TextView conform = view.findViewById(R.id.dialog_conform);
            TextView title = view.findViewById(R.id.tv_dialog_title);
            title.setText("重命名目录（名字别太长>_<）");
            final EditText dir_name = view.findViewById(R.id.edit_dir_name);
            dir_name.setText(name);
            //设置对话框的大小
            view.setMinimumHeight((int) (ScreenSizeUtil.getScreenHeight(context) * 0.2f));
            //设置按钮事件
            //取消按钮
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            //确定按钮
            conform.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //获取输入的内容
                    String newName = dir_name.getText().toString().trim();
                    if(TextUtils.isEmpty(newName)){
                        Toast.makeText(view.getContext(), "请输入", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //检测是否合法
                    if(Util.checkFileName(newName)){//名字合法
                        File oldFile = new File(Data.filesPath,name);
                        File newFile = new File(Data.filesPath,newName);
                        if(newFile.exists()){
                            ToastUtil.errorToast(view.getContext(),"目录已存在");
                            return;
                        }
                        boolean flag = oldFile.renameTo(newFile);
                        if(flag){
                            ToastUtil.myToast(view.getContext(),"成功");
                            //刷新
                            refreshDirs_list();
                            refreshDirs_screen();
                        }else {
                            ToastUtil.errorToast(view.getContext(),"失败");
                        }
                        dialog.dismiss();
                    }else {
                        Toast.makeText(view.getContext(), "文件名不能包含下列任何字符：\n\\ / : * ? \" < > |\n且首个字符不能为.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            //设置自定义View
            dialog.setContentView(view);
            dialog.show();
            //设置获取焦点时全选
            dir_name.setSelectAllOnFocus(true);
            dir_name.requestFocus();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) dir_name.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(dir_name,InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            },160);
        }

    }



    @Override
    public void onBackPressed() {
        //首先检测抽屉是否关闭
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            //如果打开了DrawerLayout则返回键是关闭
            drawer.closeDrawers();
            return;
        }
        if ((System.currentTimeMillis() - exitTime) > 1800) {
            Snackbar.make(drawer,"重复此动作退出保险箱",Snackbar.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        }
        else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //只要此界面显示了，就刷新视图（防止误删文件夹后，又点击）
        refreshDirs_list();
        refreshDirs_screen();
    }

    public static class DirView {
        //图片
        String coverPath;
        //内容
        public String name;
        //包含多少项
        String count;

        //图片路径、内容
        DirView(String coverPath, String name, String count){
            this.coverPath = coverPath;
            this.name = name;
            this.count = count;
        }
    }
}
