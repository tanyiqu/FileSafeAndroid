package com.tanyiqu.filesafe.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.utils.FileUtil;
import com.tanyiqu.filesafe.utils.ToastUtil;
import com.tanyiqu.filesafe.utils.Util;
import com.tanyiqu.filesafe.view.NineLockView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;


public class PasswdActivity extends Activity {

    String pass;
    private static final String DEFAULT_PASSWD = "4753";
    TextView tv_msg;
    //是否是设置密码
    boolean isSetPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwd);
        init();
    }

    private void init() {
        tv_msg = findViewById(R.id.tv_msg);
        //初始化环境 文件夹等
        detect();
        //九宫格解锁
        initNineLock();
    }

    private void initNineLock() {
        NineLockView nineLockView = findViewById(R.id.nineLock);
        final File passFile = new File(Data.internalStoragePath,"passwd");
        //文件不存在则创建
        if(!passFile.exists()){
            try {
                passFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {//文件存在且大小不为0
            if(passFile.length() != 0){
                try {
                    FileReader in = new FileReader(passFile);
                    BufferedReader br = new BufferedReader(in);
                    pass = br.readLine();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(isSetPass){
            tv_msg.setText("设置图案密码");
        }else {
            tv_msg.setText("绘制图案");
        }
        nineLockView.setOnPatternChangedListener(new NineLockView.OnPatternChangedListener() {
            @Override
            public void onPatternChanged(NineLockView nineLockView, String passwd) {
                if(isSetPass){
                    //设置密码
                    //获取两次绘制的密码
                    if(passwd == null){
                        Toast.makeText(PasswdActivity.this, "至少连接4个点", Toast.LENGTH_SHORT).show();
                        nineLockView.refreshView(true);
                    }else {
                        if(pass == null){//第一次绘制
                            //记录绘制的密码
                            pass = passwd;
                            Toast.makeText(PasswdActivity.this, "请再绘制一次", Toast.LENGTH_SHORT).show();
                            nineLockView.refreshView(false);
                        }else {//第二次绘制
                            if(pass.equals(passwd)){//如果和上次绘制的一样，此密码就为新密码
                                Toast.makeText(PasswdActivity.this, "已设置密码为：" + passwd, Toast.LENGTH_SHORT).show();
                                //记录新密码
                                try {
                                    FileWriter out = new FileWriter(passFile);
                                    BufferedWriter bw = new BufferedWriter(out);
                                    bw.write(passwd);
                                    bw.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //记录完成，跳转
                                goMain();
                            }else{//否则，重新绘制两次
                                nineLockView.refreshView(false);
                                Toast.makeText(PasswdActivity.this, "密码不一致", Toast.LENGTH_SHORT).show();
                                pass = null;
                            }
                        }
                    }

                }else{
                    //检测密码
                    if(passwd == null){
                        ToastUtil.errorToast(PasswdActivity.this,"至少连接4个点");
                        nineLockView.setWrong();
                        nineLockView.refreshView(true);
                    }else {
                        if (passwd.equals(pass)){
                            //密码正确
                            ToastUtil.myToast(PasswdActivity.this,"欢迎回来");
                            goMain();
                        }else {
                            //密码错误
                            ToastUtil.errorToast(PasswdActivity.this,"密码错误");
                            //震动一下
                            Util.vibrate(PasswdActivity.this,100);
                            nineLockView.setWrong();
                            nineLockView.refreshView(false);
                        }
                    }
                }
            }
        });
    }

    private void detect() {
        //如果标志文件已存在，跳过这一步
        //标识文件
        //标识文件存放在 内部存储中，如果卸载再重装了，可以有提示
        final File flagFile1 = new File(Data.internalStoragePath,"flag");
        File flagFile2 = new File(Data.externalStoragePath+ "/.file_safe","flag");
        File passFile = new File(Data.internalStoragePath,"passwd");
        //是否需要设置密码
        if(!passFile.exists() || passFile.length()==0){//密码文件不存在或者大小为0时设置
            isSetPass = true;
        }else {
            isSetPass = false;
        }
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
                            try {
                                flagFile1.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
        File passFile = new File(Data.internalStoragePath,"passwd");
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
            if(!passFile.exists()){
                flag = passFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!filesDir.exists()){
            flag = filesDir.mkdir();
        }

        //默认分出文件夹
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
        DirsActivity.refreshDirs_list();

    }

    public void goMain(){
        startActivity(new Intent(this, DirsActivity.class));
        finish();
    }

}
