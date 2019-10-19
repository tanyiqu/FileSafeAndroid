package com.tanyiqu.filesafe.data;

import android.Manifest;
import android.util.Log;

import com.tanyiqu.filesafe.adapter.FilesAdapter;
import com.tanyiqu.filesafe.fragment.DirsFragment;
import com.tanyiqu.filesafe.fragment.FilesFragment;

import java.util.List;

public class Data {

    //内部存储路径
    public static String internalStoragePath;   //在SplashActivity获取
    //储存卡根目录路径
    public static String externalStoragePath;   //在SplashActivity获取,如果开始没有权限则在permissionActivity获取
    //存储加密文件的files目录路径
    public static String filesPath;             //同上
    //权限

    public static String[] perms_storage = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static String[] perms_camera = new String[]{Manifest.permission.CAMERA};
    //目录视图      列表
    public static List<DirsFragment.DirView> dirViewList;
    //文件视图      列表
    public static List<FilesAdapter.FileView> fileViewList;

    /**
     * 以下函数为赋值函数
     */

    public static void setExternalStoragePath(String externalStoragePath) {
        Data.externalStoragePath = externalStoragePath;
//        Log.i("MyApp","已配置 externalStoragePath" + Data.externalStoragePath);
    }

    public static void setDirViewList(List<DirsFragment.DirView> dirViewList) {
        Data.dirViewList = dirViewList;
    }

    public static void setInternalStoragePath(String internalStoragePath) {
        Data.internalStoragePath = internalStoragePath;
        Log.i("MyApp","已配置 internalStoragePath：" + Data.internalStoragePath);
    }

    public static void setFilesPath(String filesPath) {
        Data.filesPath = filesPath;
    }
}
