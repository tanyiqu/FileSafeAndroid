package com.tanyiqu.filesafe.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.activity.MainActivity;
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.exception.NoSuchFileToPlayException;
import com.tanyiqu.filesafe.utils.FileUtil;
import com.tanyiqu.filesafe.utils.ScreenSizeUtil;
import com.tanyiqu.filesafe.utils.ToastUtil;
import com.tanyiqu.filesafe.utils.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DirsFragment extends Fragment {

    public static RecyclerView recycler = null;

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
        toolbar.inflateMenu(R.menu.menu_toolbar);
        //设置menu点击事件
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_add_dir:
//                        ToastUtil.myToast(toolbar.getContext(),"新建文件夹");
                        mkDir(toolbar.getContext());
                        break;
                    case R.id.action_grid:
                        ToastUtil.myToast(toolbar.getContext(),"网格模式");
                        break;
                    case R.id.action_list:
                        ToastUtil.myToast(toolbar.getContext(),"列表模式");
                        break;
                    case R.id.action_play:

                        try {
                            Util.openVideoFile(toolbar.getContext(),new File(Data.externalStoragePath + "/下载/test.mp4"));
                        } catch (NoSuchFileToPlayException e) {
                            ToastUtil.errorToast(toolbar.getContext(),e.getMessage());
                        }

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
        DirsAdapter adapter = new DirsAdapter(Data.dirViewList,getFragmentManager());
        recycler.setAdapter(adapter);
    }

    private void mkDir(Context context) {
        //弹出一个对话框
        final Dialog dialog = new Dialog(context, R.style.NormalDialogStyle);
        View view = View.inflate(context, R.layout.layout_dialog_mkdir, null);
        TextView dialog_cancel = view.findViewById(R.id.dialog_cancel);
        TextView dialog_conform = view.findViewById(R.id.dialog_conform);
        final EditText edit_dir_name = view.findViewById(R.id.edit_dir_name);
//        edit_dir_name.requestFocus();
        // 设置自定义的布局
        dialog.setContentView(view);
        //使得点击对话框外部不消失对话框
        dialog.setCanceledOnTouchOutside(true);
        //设置对话框的大小
        view.setMinimumHeight((int) (ScreenSizeUtil.getScreenHeight(context) * 0.2f));
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
                    Toast.makeText(view.getContext(), "文件名不能包含下列任何字符：\n\\ / : * ? \" < > |", Toast.LENGTH_LONG).show();
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
                BitmapDrawable d = (BitmapDrawable) res.getDrawable(R.mipmap.other);
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
                Data.dirViewList.add(new DirView(coverPath, dirName));
                DirsAdapter adapter = new DirsAdapter(Data.dirViewList, getFragmentManager());
                recycler.setAdapter(adapter);
                ToastUtil.myToast(view.getContext(), "新建成功！");
            }
        });
        dialog.show();


    }

    //根据配置文件夹 刷新
    public static void refreshDirs(){
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
        Data.setDirViewList(dirViewList);
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
    public class DirsAdapter extends RecyclerView.Adapter<DirsAdapter.ViewHolder>{

        private List<DirView> dirViewList;
        private FragmentManager fragmentManager;
        private final String[] options = new String[]{"重命名","删除","导出","设置封面"};
        private final static int DEFAULT_SELECT = 0;
        private int currSelectedOption = DEFAULT_SELECT;

        public DirsAdapter(List<DirView> dirViewList, FragmentManager fragmentManager){
            this.dirViewList = dirViewList;
            this.fragmentManager = fragmentManager;
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
            final DirView dirView = dirViewList.get(position);

            //添加图片和内容
            Bitmap bmp= BitmapFactory.decodeFile(dirView.coverPath);
            holder.img_dirs_item.setImageBitmap(bmp);
            //内容
            holder.tv_dirs_content.setText(dirView.content);
            //路径
            holder.path = Data.externalStoragePath + File.separator + ".file_safe" + File.separator  + "files" + File.separator + dirView.content;

            //设置点击事件
            holder.dirs_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //确定点击的是哪一个
                    String path = Data.externalStoragePath + File.separator + ".file_safe" + File.separator + "files" + File.separator + dirView.content;

                    Toast.makeText(view.getContext(), path, Toast.LENGTH_SHORT).show();

                    File dir = new File(path);
                    if(!dir.exists()){
                        Toast.makeText(view.getContext(), "目录已经不存在了", Toast.LENGTH_SHORT).show();
                        //刷新一下
                        for(DirView v : Data.dirViewList){
                            if(v.content.contentEquals(dirView.content)){
                                Data.dirViewList.remove(v);
                                break;
                            }
                        }
                        DirsAdapter adapter = new DirsAdapter(Data.dirViewList,fragmentManager);
                        DirsFragment.recycler.setAdapter(adapter);
                        return;
                    }
                    //点击Dir的时候，构造好一个 FileView 的 List
                    FilesFragment.refreshFileView(path);
                    //切换fragment
                    FilesFragment fragment = new FilesFragment(path,dirView.content);
                    fragmentManager.beginTransaction()
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
                    builder.setTitle(dirView.content);

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

                                    break;
                                case 1://删除
                                    //删除 dirView.content
                                    rmDirView(context,dirView.content);
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
                            refreshDirs();
                            DirsFragment.recycler.setAdapter(new DirsAdapter(Data.dirViewList, MainActivity.fragmentManager));
                            if (flag){
                                ToastUtil.myToast(context,"删除成功");
                            }else {
                                ToastUtil.errorToast(context,"删除失败");
                            }
                        }
                    })
                    .show();
        }

    }

    public static class DirView {
        //图片
        public String coverPath;
        //内容
        public String content;

        //图片路径、内容
        public DirView(String coverPath,String content){
            this.coverPath = coverPath;
            this.content = content;
        }

    }


}
