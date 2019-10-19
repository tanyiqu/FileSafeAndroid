package com.tanyiqu.filesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.data.Data;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class PermissionActivity extends Activity implements  EasyPermissions.PermissionCallbacks{

    static final int CALL_BACK_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        init();
    }

    private void init() {
        if(EasyPermissions.hasPermissions(this, Data.perms_storage)){
            Toast.makeText(this, "已经有存储权限", Toast.LENGTH_SHORT).show();
        }else {
            EasyPermissions.requestPermissions(this,"快同意存储权限申请\n快同意存储权限申请\n快同意存储权限申请", CALL_BACK_STORAGE, Data.perms_storage);
        }
    }

    public void startApp(){
        if(EasyPermissions.hasPermissions(this, Data.perms_storage)){
            Data.setExternalStoragePath(Environment.getExternalStorageDirectory().getAbsolutePath());
            Data.setFilesPath(Data.externalStoragePath + File.separator + ".file_safe" + File.separator + "files");
            startActivity(new Intent(this,PasswdActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode){
            case CALL_BACK_STORAGE:
                Toast.makeText(this, "已同意存储权限", Toast.LENGTH_SHORT).show();
                startApp();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        switch (requestCode){
            case CALL_BACK_STORAGE:
                Toast.makeText(this, "已拒绝存储权限", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, Data.perms_storage,CALL_BACK_STORAGE);
                break;
        }
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            Toast.makeText(this, "权限已被永久拒绝", Toast.LENGTH_SHORT).show();
            new AppSettingsDialog
                    .Builder(this)
                    .setTitle("权限已被永久拒绝")
                    .setRationale("该应用需要此权限，否则无法正常使用，是否打开设置")
                    .setPositiveButton("嗯嗯")
                    .setNegativeButton("嘤嘤嘤")
                    .build()
                    .show();
        }
    }
}
