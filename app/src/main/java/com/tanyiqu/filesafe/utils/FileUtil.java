package com.tanyiqu.filesafe.utils;

import java.io.File;

public class FileUtil {



    //递归删除文件夹 及其文件
    public static boolean deleteDir_r(File file) {
        if (file.isFile()) {
            return file.delete();
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                return file.delete();
            }
            for (File f : childFile) {
                deleteDir_r(f);
            }
            return file.delete();
        }
        return true;
    }
}
