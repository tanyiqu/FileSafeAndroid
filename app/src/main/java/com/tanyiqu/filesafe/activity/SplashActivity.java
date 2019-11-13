package com.tanyiqu.filesafe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.data.Data;

import java.io.File;

import pub.devrel.easypermissions.EasyPermissions;

public class SplashActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        //程序打开时传递内部存储路径
        Data.setInternalStoragePath(getFilesDir().getAbsolutePath());

        if(EasyPermissions.hasPermissions(this, Data.perms_storage)){
            //有权限，传递路径
            //外部存储路径
            Data.setExternalStoragePath(Environment.getExternalStorageDirectory().getAbsolutePath());
            //加密文件目录路径
            Data.setFilesPath(Data.externalStoragePath + File.separator + ".file_safe" + File.separator + "files");
            //导出目录路径
            Data.setExportPath(Data.externalStoragePath + File.separator + "FileSafe" + File.separator + "Export");

            new Handler().postDelayed(new Runnable(){
                public void run() {
                    finish();
                    startActivity(new Intent(SplashActivity.this, PasswdActivity.class));
                }
            }, 1000);
        }else{
            //无权限，启动请求权限界面
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    startActivity(new Intent(SplashActivity.this, PermissionActivity.class));
                }
            }, 1000);
        }

    }

}
