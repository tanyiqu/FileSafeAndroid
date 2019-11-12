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
import com.tanyiqu.filesafe.bean.SettingBean;
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.utils.FileUtil;
import com.tanyiqu.filesafe.utils.ToastUtil;
import com.tanyiqu.filesafe.utils.Util;
import com.tanyiqu.filesafe.view.NineLockView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class PasswdActivity extends Activity {

    String pass;
    TextView tv_msg;

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

    /**
     * 初始化解锁视图
     */
    private void initNineLock() {
        NineLockView nineLockView = findViewById(R.id.nineLock);

        pass = Data.setting.getPasswd();
        //如果设置对象的密码为""则需要设置密码
        final boolean isSetPass = pass.equals("");

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
                    //少于4个点
                    if(passwd == null){
                        Toast.makeText(PasswdActivity.this, "至少连接4个点", Toast.LENGTH_SHORT).show();
                        nineLockView.refreshView(true);
                    }
                    //不少于4个点
                    else {
                        //第一次绘制
                        if(pass.equals("")){
                            //记录绘制的密码
                            pass = passwd;
                            Toast.makeText(PasswdActivity.this, "请再绘制一次", Toast.LENGTH_SHORT).show();
                            nineLockView.refreshView(false);
                        }
                        //第二次绘制
                        else {
                            if(pass.equals(passwd)){//如果和上次绘制的一样，此密码就为新密码
                                Toast.makeText(PasswdActivity.this, "已设置密码为：" + passwd, Toast.LENGTH_SHORT).show();
                                //记录新密码
                                Data.setting.setPasswd(passwd);
                                Util.syncSettingToFile(new File(Data.internalStoragePath,"config.json"));
                                //记录完成，跳转
                                goMain();
                            }else{//否则，重新绘制两次
                                nineLockView.refreshView(false);
                                Toast.makeText(PasswdActivity.this, "密码不一致", Toast.LENGTH_SHORT).show();
                                pass = "";
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

    /**
     * 检测
     */
    private void detect(){
        //先判断是否为首次启动
        File config = new File(Data.internalStoragePath,"config.json");
        final File flagFile = new File(Data.externalStoragePath + File.separator + ".file_safe","flag");
        boolean isFirstStart = !config.exists();
        //是首次启动
        if(isFirstStart){
            //只要是首次启动，就一定先实例化setting对象
            //密码为空表示需要设置密码
            Data.setting = new SettingBean("",SettingBean.DIRS_ORDER_ASCENDING);
            //同步配置到json文件里
            Util.syncSettingToFile(config);

            //判断之前是否安装过
            boolean onceInstalled = flagFile.exists();
            if(onceInstalled){//曾经安装过
                //提示曾经的文件是否保留
                AlertDialog builder = new AlertDialog.Builder(this)
                        .setMessage("检测到上次卸载前加密的文件\n是否恢复？")
                        //恢复
                        .setPositiveButton("吓死我了，快恢复", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //一定要创建flag文件，否则会一直弹出
                                try {
                                    boolean flag = flagFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        //不恢复
                        .setNegativeButton("什么玩意，删了吧", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //删掉文件夹，并且重新创建配置文件
                                File dir = new File(Data.externalStoragePath+ "/.file_safe");
                                FileUtil.deleteDir_r(dir);
                                createIniFiles();
                            }
                        }).create();
                builder.setCancelable(false);
                builder.show();
            }else {//首次安装
                //创建文件
                createIniFiles();
            }
        }else{//不是首次启动，相关文件一定齐全
            //读取设置到设置对象
            Util.getSettingFromFile(config);
        }
    }

    /**
     * 创建相关文件
     */
    private void createIniFiles(){
        boolean flag;//无用，单纯用来消黄色警告
        //配置文件
        File config = new File(Data.internalStoragePath,"config.json");
        File flagFile = new File(Data.externalStoragePath + File.separator + ".file_safe","flag");
        File warningFile = new File(Data.externalStoragePath + File.separator + ".file_safe","_此文件夹下的文件非常重要，请勿删除！_");
        //加密文件的目录
        File root = new File(Data.externalStoragePath + File.separator + ".file_safe");
        File files = new File(Data.filesPath);

        //创建基本文件
        try {
            flag = config.createNewFile();
            flag = root.mkdir();
            flag = files.mkdir();
            flag = flagFile.createNewFile();
            flag = warningFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //默认分出两个文件夹
        File picDir = new File(Data.filesPath,"图片");
        File videoDir = new File(Data.filesPath,"视频");
        if(!picDir.exists()) flag = picDir.mkdir();
        if(!videoDir.exists()) flag = videoDir.mkdir();
        //依次添加上默认封面
        Resources res = getResources();
        BitmapDrawable[] pics = new BitmapDrawable[]{
                (BitmapDrawable) res.getDrawable(R.mipmap.cover_pic),
                (BitmapDrawable) res.getDrawable(R.mipmap.cover_video)
        };
        String fn = "cover.jpg";
        String[] paths = new String[]{
                picDir.getPath() + File.separator + fn,
                videoDir.getPath() + File.separator + fn
        };
        Bitmap[] imgs = new Bitmap[]{
                pics[0].getBitmap(),
                pics[1].getBitmap()
        };
        try {
            for (int i=0;i<paths.length;i++){
                OutputStream os = new FileOutputStream(paths[i]);
                imgs[i].compress(Bitmap.CompressFormat.JPEG,100,os);
                os.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        //依次创建一个数据文件
        String ini = "data.db";
        File[] iniFiles = new File[]{
                new File(picDir.getPath(),ini),
                new File(videoDir.getPath(),ini)
        };
        for(File iniFile :iniFiles){
            try{
                flag = iniFile.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        Toast.makeText(this, "配置文件创建成功！", Toast.LENGTH_SHORT).show();
    }

    /**
     * 启动主界面
     */
    public void goMain(){
        startActivity(new Intent(this, DirsActivity.class));
        finish();
    }

}
