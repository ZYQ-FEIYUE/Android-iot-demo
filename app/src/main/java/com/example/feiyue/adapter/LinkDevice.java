package com.example.feiyue.adapter;

public class LinkDevice {
    private String name;    //设备名称
    private String mode;
    private int imageId;    //设备图片资源
    private String deviceBssid; //设备mac
    private String APBssid;     //设备连接所连接路由mac
    public LinkDevice(String name, String mode, int imageId, String deviceBssid, String APBssid) {
        this.name = name;
        this.mode = mode;
        this.imageId = imageId;
        this.deviceBssid = deviceBssid;
        this.APBssid = APBssid;
    }
    public String getDeviceBssid() {
        return deviceBssid;
    }
    public String getAPBssid() {
        return APBssid;
    }
    public String getName() {
        return name;
    }
    public int getImageId() {
        return imageId;
    }
    public String getMode() {
        return mode;
    }
}
