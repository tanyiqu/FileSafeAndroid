package com.tanyiqu.filesafe.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.exception.NoSuchFileToOpenException;

import java.io.File;

public class FileUtil {

    /**
     * 调用系统应用打开文件
     * @param context context
     * @param file file对象
     * @param ext 扩展名
     * @throws NoSuchFileToOpenException 没有文件异常
     */
    public static void openFile(Context context, File file,String ext) throws NoSuchFileToOpenException {

        if(! file.exists()){
            throw new NoSuchFileToOpenException("文件不存在");
        }

        //根据扩展名，适配相应的type
        String type = getType(ext);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".FileProvider", file);
            intent.setDataAndType(contentUri,type);
        } else {
            Uri uri = Uri.fromFile(file);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri,type);
        }
        context.startActivity(intent);
    }

    public static String getFileExt(String fileName){
        String ext = "";
        int pos = fileName.lastIndexOf(".");
        ext = fileName.substring(pos+1,fileName.length());
        return ext.toLowerCase();
    }

    public static int getImgId(String ext){
        switch (ext){
            case "apk":
                return R.mipmap.ic_file_apk;
            //压缩包
            case "zip":
            case "rar":
            case "tar":
            case "7z":
                return R.mipmap.ic_file_archive;
            //视频
            case "avi":
            case "mp4":
            case "mpeg":
            case "mov":
            case "wmv":
                return R.mipmap.ic_file_video;
            //音频
            case "mp3":
            case "wav":
            case "wma":
                return R.mipmap.ic_file_audio;
            //图片
            case "bmp":
            case "png":
            case "jpg":
            case "jpeg":
            case "ico":
            case "gif":
                return R.mipmap.ic_file_pic;
            //文本
            case "txt":
            case "log":
                return R.mipmap.ic_file_txt;
            //文档
            case "doc":
            case "docx":
                return R.mipmap.ic_file_word;
            case "xls":
            case "xlsx":
                return R.mipmap.ic_file_excel;
            case "ppt":
            case "pptx":
                return R.mipmap.ic_file_ppt;
            case "pdf":
                return R.mipmap.ic_file_pdf;
            //代码
            case "html":
            case "xml":
                return R.mipmap.ic_file_code;
            //默认
            case "":
            default:
                return R.mipmap.ic_file_default;
        }
    }

    private static String getType(String ext) {
        switch (ext){
            //视频类
            case "mp4":
                return "video/*";
            //图片类
            case "png":
            case "jpg":
            case "jpeg":
                return "image/*";
            case "gif":
                return "image/gif";
            //音频类
            case "mp3":
                return "audio/*";
            //文本类
            case "txt":
                return "text/*";
            //压缩包类
            case "zip":
                return "application/x-zip-compressed";
            //类
            //类
            //类
            //类
            //默认
            case "":
            default:
                return "*/*";
        }
    }

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
