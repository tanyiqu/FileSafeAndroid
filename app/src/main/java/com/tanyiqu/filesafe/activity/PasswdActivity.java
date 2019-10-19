package com.tanyiqu.filesafe.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.fragment.DirsFragment;
import com.tanyiqu.filesafe.utils.FileUtil;
import com.tanyiqu.filesafe.utils.ToastUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class PasswdActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwd);
        init();
    }

    private void init() {
        //初始化环境 文件夹等
        detect();
        //初始化文件夹视图列表
        //现在创建视图 可能会有 上次安装未删净的文件继续显示的 bug
//        initDirsView();
    }

    private void detect() {
        //如果标志文件已存在，跳过这一步
        //标识文件
        //标识文件存放在 内部存储中，如果卸载再重装了，可以有提示
        File flagFile1 = new File(Data.internalStoragePath,"flag");
        File flagFile2 = new File(Data.externalStoragePath+ "/.file_safe","flag");
        if(flagFile1.exists()){//如果此文件存在则一定 非首次开启
            //非首次开启
            initDirsView();
            return;
        }
        //现在是首次开启，判断之前是否安装过
        if(flagFile2.exists()){
            //之前安装过
            //提示恢复文件 或 清除文件
            AlertDialog builder = new AlertDialog.Builder(this)
                    .setMessage("检测到上次卸载前加密的文件\n是否恢复？")
                    .setPositiveButton("吓死我了，快恢复", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            initDirsView();
                        }
                    })//恢复操作相当于什么也不做
                    .setNegativeButton("什么玩意，删了吧", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //删掉文件夹，并且重新创建配置文件
                            File dir = new File(Data.externalStoragePath+ "/.file_safe");

                            FileUtil.deleteDir_r(dir);

                            createIniFiles();
                            initDirsView();
                        }
                    })//删除操作就是把".file_safe"删掉
                    .create();
            builder.setCancelable(false);
            builder.show();
            return;
        }
        createIniFiles();
        initDirsView();
    }

    private void createIniFiles(){
        //首次开启并且首次安装
        File flagFile1 = new File(Data.internalStoragePath,"flag");
        File flagFile2 = new File(Data.externalStoragePath + File.separator + ".file_safe","flag");

        boolean flag = false;
        String rootPath = Data.externalStoragePath + File.separator + ".file_safe";         //根路径
        String filesPath = Data.filesPath;             //存放文件的路径 (files文件夹路径)

        //存储用文件夹
        File rootDir = new File(Data.externalStoragePath,".file_safe");
        //警告文件
        File warningFile = new File(rootPath,"_此文件夹下的文件非常重要，请勿删除！_");

        //存放文件的文件夹
        File filesDir = new File(rootPath,"files");

        if(!rootDir.exists()){//文件夹不存在则创建
            flag = rootDir.mkdir();
        }
        try {
            if(!warningFile.exists()){//文件不存在则创建
                flag = warningFile.createNewFile();
            }
            if(!flagFile1.exists()){
                flag = flagFile1.createNewFile();
            }
            if(!flagFile2.exists()){
                flag = flagFile2.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!filesDir.exists()){
            flag = filesDir.mkdir();
        }

        //默认分出5个文件夹 图片、视频、音频、文档、其他
        File picDir = new File(filesPath,"图片");
        File videoDir = new File(filesPath,"视频");
        if(!picDir.exists()) flag = picDir.mkdir();
        if(!videoDir.exists()) flag = videoDir.mkdir();

        //依次添加上默认封面
        Resources res = getResources();
        BitmapDrawable[] pics = new BitmapDrawable[]{
                (BitmapDrawable) res.getDrawable(R.mipmap.pic),
                (BitmapDrawable) res.getDrawable(R.mipmap.video)
        } ;
        Bitmap[] imgs = new Bitmap[]{
                pics[0].getBitmap(),
                pics[1].getBitmap()
        };
        String fn = "cover.jpg";
        String[] path = new String[] {
                picDir.getPath() + File.separator + fn,
                videoDir.getPath() + File.separator + fn};
        try{
            for(int i=0;i<path.length;i++){
                OutputStream os = new FileOutputStream(path[i]);
                imgs[i].compress(Bitmap.CompressFormat.JPEG,100,os);
                os.close();
            }
        } catch (FileNotFoundException e1){
            Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT).show();
        } catch (IOException e2) {
            Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show();
        }

        //依次创建一个数据文件
        String ini = "data.db";
        File[] iniFiles = new File[]{
                new File(picDir.getPath(),ini), new File(videoDir.getPath(),ini)
        };
        for (File iniFile : iniFiles){
            try {
                flag = iniFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(this, "配置文件创建成功", Toast.LENGTH_SHORT).show();
    }

    private void initDirsView() {
        //使用配置文件初始化
        List<DirsFragment.DirView> dirViewList = new ArrayList<DirsFragment.DirView>();
        DirsFragment.refreshDirs();
    }

    public void f1(View view){
        ToastUtil.myToast(this,"欢迎回来");
        goMain();
    }

    public void f2(View view){
        ToastUtil.errorToast(this,"密码错误");
        //震动一下
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(100);
        }
    }

    //调用前摄像头拍照
    public void f3(View view){
        //申请权限
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(Data.perms_camera,11);
//        }

    }

    public void goMain(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}
