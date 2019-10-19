package com.tanyiqu.filesafe.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.tanyiqu.filesafe.exception.NoSuchFileToPlayException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Util {

    //以视频方式打开文件
    public static void openVideoFile(Context context, File file) throws NoSuchFileToPlayException {

        if(! file.exists()){
            throw new NoSuchFileToPlayException("文件不存在");
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".FileProvider", file);

//            Toast.makeText(context, contentUri.getPath(), Toast.LENGTH_SHORT).show();

            intent.setDataAndType(contentUri, "video/*");
        } else {
            Uri uri = Uri.fromFile(file);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "video/*");
        }

        context.startActivity(intent);
    }

    //随机生成 [0,n] 的随机数
    public static int RandomInt(int n){
        Random r = new Random();
        return r.nextInt(n+1);
    }

    //随机生成 [n,m] 的随机数
    public static int RandomInt(int n,int m){
        Random r = new Random();
        int s = r.nextInt(m);   //[0,m)
        int rest = m - n + 1;
        s = s % rest;           //[0,rest]
        s += n;                 //[n,m]
        return s;
    }

    //随机文件名字
    public static String RandomName(){
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<10;i++){
            int r = RandomInt(0,str.length());
            sb.append(str.charAt(r));
        }
        return sb.toString();
    }

    public static boolean checkFileName(String name){
        if(name.contains("\\")){ return false;}
        if(name.contains("/")){ return false;}
        if(name.contains(":")){ return false;}
        if(name.contains("*")){ return false;}
        if(name.contains("?")){ return false;}
        if(name.contains("\"")){ return false;}
        if(name.contains("<")){ return false;}
        if(name.contains(">")){ return false;}
        return !name.contains("|");
    }

    public static boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }

            /* 如果不需要打log，可以使用下面的语句
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            */

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateIni(File iniFile,String orName,String enName,long date,long size){

        try {
            FileWriter out = new FileWriter(iniFile,true);//追加
            BufferedWriter bw = new BufferedWriter(out);
            bw.write(orName);bw.write("#");
            bw.write(enName);bw.write("#");
            bw.write(transferLongToDate(date));bw.write("#");
            bw.write(byteToSize(size));bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //字节数转大小
    public static String byteToSize(long size) {
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB   ";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB   ";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB   ";
        } else {
            resultSize = size + "B   ";
        }
        return resultSize;
    }

    //毫秒数转日期
    public static String transferLongToDate(Long millSec){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date data = new Date(millSec);
        return sdf.format(data);
    }

}
