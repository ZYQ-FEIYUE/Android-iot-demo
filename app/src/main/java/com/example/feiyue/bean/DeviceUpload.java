package com.example.feiyue.bean;

import java.util.List;

public class DeviceUpload {
    private String deviceMac;
    private int[] deviceIp;
    private boolean power;
    private int Red;
    private int Green;
    private int Blue;

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public int[] getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(int[] deviceIp) {
        this.deviceIp = deviceIp;
    }

    public boolean isPower() {
        return power;
    }

    public void setPower(boolean power) {
        this.power = power;
    }

    public int getRed() {
        return Red;
    }

    public void setRed(int red) {
        Red = red;
    }

    public int getGreen() {
        return Green;
    }

    public void setGreen(int green) {
        Green = green;
    }

    public int getBlue() {
        return Blue;
    }

    public void setBlue(int blue) {
        Blue = blue;
    }
}
