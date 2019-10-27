package com.tanyiqu.filesafe.fragment;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.activity.AboutActivity;
import com.tanyiqu.filesafe.activity.MainActivity;
import com.tanyiqu.filesafe.activity.SettingActivity;
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

public class DirsFragment extends Fragment {

    public static RecyclerView recycler = null;
    static DirsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dirs,container,false);

        //初始化ToolBar
        initToolBar(root);
        //初始化RecyclerView
        initRecycler(root);
        return root;

    }

    private void initToolBar(final View root) {
        final Toolbar toolbar = root.findViewById(R.id.toolbar_dirs);
        //设置标题
        toolbar.setTitle("保险箱");
        //设置导航Button点击事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.drawer.openDrawer(GravityCompat.START);
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
                        startActivity(new Intent(getActivity(), SettingActivity.class));
                        break;
                    case R.id.action_about://关于界面
                        startActivity(new Intent(getActivity(), AboutActivity.class));
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void initRecycler(View root) {
        recycler = root.findViewById(R.id.recycler_dirs);
        GridLayoutManager layoutManager = new GridLayoutManager(root.getContext(),2);
        recycler.setLayoutManager(layoutManager);
        adapter = new DirsAdapter(Data.dirViewList);
        recycler.setAdapter(adapter);
    }

    private void mkDir(Context context) {
        final Dialog dialog = Util.inputDialog(context);
        View view = View.inflate(context, R.layout.layout_dialog_input, null);
        TextView dialog_cancel = view.findViewById(R.id.dialog_cancel);
        TextView dialog_conform = view.findViewById(R.id.dialog_conform);
        TextView tv_dialog_title = view.findViewById(R.id.tv_dialog_title);
        tv_dialog_title.setText("新建目录");
        dialog_conform.setText("新建");
        final EditText edit_dir_name = view.findViewById(R.id.edit_dir_name);
        //设置对话框的大小
        view.setMinimumHeight((int) (ScreenSizeUtil.getScreenHeight(context) * 0.2f));
        //取消按钮
        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        //新建按钮
        dialog_conform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取目录名并去掉多余空格
                String dirName = edit_dir_name.getText().toString().trim();
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
                DirsFragment.refreshDirs_list();
                DirsFragment.refreshDirs_screen();
                ToastUtil.myToast(view.getContext(), "新建成功！");
            }
        });
        // 设置自定义的布局
        dialog.setContentView(view);
        dialog.show();
        //弹出输入法
        edit_dir_name.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) edit_dir_name.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edit_dir_name,InputMethodManager.SHOW_IMPLICIT);
            }
        },150);
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
                    dirViewList.add(new DirView(d.getPath() + "/cover.jpg",name));
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

    public static void refreshDirs_screen(){
        adapter = new DirsAdapter(Data.dirViewList);
        recycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        //需要两次点击返回
        MainActivity.neededDoubleClickToExit = true;
        //解锁抽屉滑动
        MainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onPause() {
        super.onPause();
        //不需要两次点击返回
        MainActivity.neededDoubleClickToExit = false;
        //锁定抽屉滑动
        MainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    /**
     * Adapter
     */
    public static class DirsAdapter extends RecyclerView.Adapter<DirsAdapter.ViewHolder>{

        private List<DirView> dirViewList;
        private final String[] options = new String[]{"重命名","删除","导出","设置封面"};
        private final static int DEFAULT_SELECT = 0;
        private int currSelectedOption = DEFAULT_SELECT;

        public DirsAdapter(List<DirView> dirViewList){
            this.dirViewList = dirViewList;
        }

        //Holder类
        class ViewHolder extends RecyclerView.ViewHolder{
            CardView dirs_item;
            ImageView img_dirs_item;
            TextView tv_dirs_content;
            String path;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.dirs_item = itemView.findViewById(R.id.dirs_item);
                this.img_dirs_item = itemView.findViewById(R.id.img_dirs_item);
                this.tv_dirs_content = itemView.findViewById(R.id.tv_dirs_content);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_dirs_item_grid,parent,false);
            return new ViewHolder(root);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            //获取数据内容对象
            final DirView item = dirViewList.get(position);

            //添加图片和内容
            Bitmap bmp= BitmapFactory.decodeFile(item.coverPath);
            holder.img_dirs_item.setImageBitmap(bmp);
            //内容
            holder.tv_dirs_content.setText(item.name);
            //路径
            holder.path = Data.externalStoragePath + File.separator + ".file_safe" + File.separator  + "files" + File.separator + item.name;

            //设置点击事件
            holder.dirs_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //确定点击的是哪一个
                    String path = Data.externalStoragePath + File.separator + ".file_safe" + File.separator + "files" + File.separator + item.name;

                    File dir = new File(path);
                    if(!dir.exists()){
                        Toast.makeText(view.getContext(), "目录已经不存在了", Toast.LENGTH_SHORT).show();
                        //刷新一下
                        DirsFragment.refreshDirs_list();
                        DirsFragment.refreshDirs_screen();
                        return;
                    }
                    //点击Dir的时候，构造好一个 FileView 的 List
                    FilesFragment.refreshFileView_list(path);
                    //切换fragment
                    FilesFragment fragment = new FilesFragment(path,item.name);
                    MainActivity.fragmentManager.beginTransaction()
                            //动画效果
//                        .setCustomAnimations(R.anim.anim_fragment_right_in, R.anim.anim_fragment_left_out, R.anim.anim_fragment_left_in, R.anim.anim_fragment_right_out)
                            .addToBackStack(null)
                            .replace(R.id.fragment_container,fragment)
                            .commit();
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
//                        Toast.makeText(context, i + "", Toast.LENGTH_SHORT).show();
                            currSelectedOption = i;
                        }
                    });

                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                        Toast.makeText(context, options[currSelectedOption], Toast.LENGTH_SHORT).show();
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

        //分离对话框的函数
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
                            DirsFragment.recycler.setAdapter(new DirsAdapter(Data.dirViewList));
                            if (flag){
                                ToastUtil.myToast(context,"删除成功");
                            }else {
                                ToastUtil.errorToast(context,"删除失败");
                            }
                        }
                    })
                    .show();
        }

        private void renameDirView(final Context context,final String name){
//            String n = Data.filesPath + File.separator + name;
//            Toast.makeText(context, "重命名：" + n, Toast.LENGTH_SHORT).show();
            //弹出对话框
            final Dialog dialog = Util.inputDialog(context);
            View view = View.inflate(context, R.layout.layout_dialog_input, null);
            TextView dialog_cancel = view.findViewById(R.id.dialog_cancel);
            TextView dialog_conform = view.findViewById(R.id.dialog_conform);
            TextView tv_dialog_title = view.findViewById(R.id.tv_dialog_title);
            tv_dialog_title.setText("重命名目录");
            final EditText edit_dir_name = view.findViewById(R.id.edit_dir_name);
            edit_dir_name.setText(name);
            //设置对话框的大小
            view.setMinimumHeight((int) (ScreenSizeUtil.getScreenHeight(context) * 0.2f));
            //设置按钮事件
            //取消按钮
            dialog_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            //确定按钮
            dialog_conform.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //获取输入的内容
                    String newName = edit_dir_name.getText().toString().trim();
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
                            DirsFragment.refreshDirs_list();
                            DirsFragment.refreshDirs_screen();
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
            edit_dir_name.setSelectAllOnFocus(true);
            edit_dir_name.requestFocus();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) edit_dir_name.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(edit_dir_name,InputMethodManager.SHOW_IMPLICIT);
                }
            },160);
        }

    }

    public static class DirView {
        //图片
        public String coverPath;
        //内容
        public String name;

        //图片路径、内容
        public DirView(String coverPath,String name){
            this.coverPath = coverPath;
            this.name = name;
        }
    }

}
