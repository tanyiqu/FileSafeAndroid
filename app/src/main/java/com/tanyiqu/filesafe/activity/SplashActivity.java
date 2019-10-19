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
            //有权限，启动主界面
            Data.setExternalStoragePath(Environment.getExternalStorageDirectory().getAbsolutePath());
            Data.setFilesPath(Data.externalStoragePath + File.separator + ".file_safe" + File.separator + "files");
            startActivity(new Intent(this, PasswdActivity.class));
        }else{
            //无权限，启动请求权限界面
            startActivity(new Intent(this, PermissionActivity.class));
        }
        finish();
    }

}
