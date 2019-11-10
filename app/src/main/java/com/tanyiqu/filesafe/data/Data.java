package com.tanyiqu.filesafe.data;

import android.Manifest;
;
import com.tanyiqu.filesafe.bean.DirBean;
import com.tanyiqu.filesafe.bean.FileBean;
import com.tanyiqu.filesafe.bean.SettingBean;

import java.util.List;

public class Data {

    //内部存储路径
    public static String internalStoragePath;   //在SplashActivity获取
    //储存卡根目录路径
    public static String externalStoragePath;   //在SplashActivity获取,如果开始没有权限则在permissionActivity获取
    //存储加密文件的files目录路径
    public static String filesPath;             //在SplashActivity获取,如果开始没有权限则在permissionActivity获取
    //文件导出的目录
    public static String exportPath;            //在SplashActivity获取,如果开始没有权限则在permissionActivity获取
    //文件名中间的分割符（正则式） 用 " 来做分割 因为文件名里面一定没有“"”
    public static String Splitter = "\"";
    //设置对象
    public static SettingBean setting;

    //权限列表
    public static String[] perms_storage = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static String[] perms_camera = new String[]{Manifest.permission.CAMERA};
    //目录视图列表
    public static List<DirBean> dirBeanList;
    //文件视图列表
    public static List<FileBean> fileBeanList;


    /**
     * 设置内部存储路径
     * @param internalStoragePath 内部存储路径
     */
    public static void setInternalStoragePath(String internalStoragePath) {
        Data.internalStoragePath = internalStoragePath;
    }

    /**
     * 设置储存卡根目录路径
     * @param externalStoragePath 储存卡根目录路径
     */
    public static void setExternalStoragePath(String externalStoragePath) {
        Data.externalStoragePath = externalStoragePath;
    }

    /**
     * 设置存储加密文件的files目录路径
     * @param filesPath 存储加密文件的files目录路径
     */
    public static void setFilesPath(String filesPath) {
        Data.filesPath = filesPath;
    }

    /**
     * 设置文件导出的目录
     * @param exportPath 文件导出的目录
     */
    public static void setExportPath(String exportPath) {
        Data.exportPath = exportPath;
    }


    public static void setDirBeanList(List<DirBean> dirBeanList) {
        Data.dirBeanList = dirBeanList;
    }

    public static void setFileBeanList(List<FileBean> fileBeanList) {
        Data.fileBeanList = fileBeanList;
    }

}
