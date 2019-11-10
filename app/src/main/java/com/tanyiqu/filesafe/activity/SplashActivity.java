package com.tanyiqu.filesafe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import com.tanyiqu.filesafe.data.Data;

import java.io.File;

import pub.devrel.easypermissions.EasyPermissions;

public class SplashActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //程序打开时传递内部存储路径
        Data.setInternalStoragePath(getFilesDir().getAbsolutePath());



        if(EasyPermissions.hasPermissions(this, Data.perms_storage)){
            //有权限，传递路径
            //外部存储路径
            Data.setExternalStoragePath(Environment.getExternalStorageDirectory().getAbsolutePath());
            //加密文件目录路径
            Data.setFilesPath(Data.externalStoragePath + File.separator + ".file_safe" + File.separator + "files");
            Data.setExportPath(Data.externalStoragePath + File.separator + "FileSafe" + File.separator + "Export");
            //导出目录路径


            //启动
            startActivity(new Intent(this, PasswdActivity.class));
        }else{
            //无权限，启动请求权限界面
            startActivity(new Intent(this, PermissionActivity.class));
        }
        finish();
    }

}
