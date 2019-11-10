package com.tanyiqu.filesafe.bean;

public class SettingBean {

    //升序
    public static final String DIRS_ORDER_ASCENDING = "a-z";
    //降序
    public static final String DIRS_ORDER_DESCENDING = "z-a";

    //密码
    private String passwd;
    //目录视图排列顺序
    private String dirsOrder;

    public SettingBean() {
    }

    public SettingBean(String passwd, String dirsOrder) {
        this.passwd = passwd;
        this.dirsOrder = dirsOrder;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getDirsOrder() {
        return dirsOrder;
    }

    public void setDirsOrder(String dirsOrder) {
        this.dirsOrder = dirsOrder;
    }
}
