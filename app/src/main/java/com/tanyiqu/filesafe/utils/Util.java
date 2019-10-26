package com.tanyiqu.filesafe.utils;

import android.content.Context;
import android.os.Vibrator;

import com.tanyiqu.filesafe.data.Data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static android.content.Context.VIBRATOR_SERVICE;

public class Util {

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

    public static void updateIni(File iniFile,String orName,String enName,long date,long size){
        try {
            FileWriter out = new FileWriter(iniFile,true);//追加
            BufferedWriter bw = new BufferedWriter(out);
            bw.write(orName);bw.write(Data.Splitter);
            bw.write(enName);bw.write(Data.Splitter);
            bw.write(transferLongToDate(date));bw.write(Data.Splitter);
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

    public static void vibrate(Context context,long milliseconds){
        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(milliseconds);
        }
    }

}
