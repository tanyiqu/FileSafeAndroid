package com.tanyiqu.filesafe.bean;

public class FileBean {
    //名字
    private String original_name;    //原文件名字
    private String encrypted_name;   //加密后的名字
    //日期
    private String date;
    //大小
    private String size;
    //file 所在的路径
    private String path;

    public String getOriginal_name() {
        return original_name;
    }

    public String getEncrypted_name() {
        return encrypted_name;
    }

    public String getDate() {
        return date;
    }

    public String getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    //原文件名 加密后文件名 日期 大小 所在路径（用于打开文件）
    public FileBean(String original_name, String encrypted_name, String date, String size, String path) {
        this.original_name = original_name;
        this.encrypted_name = encrypted_name;
        this.date = date;
        this.size = size;
        this.path = path;
    }
}
