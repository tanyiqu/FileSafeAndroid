package com.tanyiqu.filesafe.Bean;

public class DirBean {
    //图片
    private String coverPath;
    //内容
    private String name;
    //包含多少项
    private String count;

    //图片路径、内容
    public DirBean(String coverPath, String name, String count){
        this.coverPath = coverPath;
        this.name = name;
        this.count = count;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public String getName() {
        return name;
    }

    public String getCount() {
        return count;
    }
}
