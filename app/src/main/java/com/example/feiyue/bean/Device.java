package com.example.feiyue.bean;

import java.util.List;

public class Device {
	private int id;			//设备识别码
	private String type;	//设备类型
	private String mode;	//设备状态
	private String user;	//主用户名称
	private String deviceMac;	//设备mac
	private String APMac;	//设备所连路由器mac
	private List<String> shares;
	public Device(int id, String type, String mode, String user, String deviceMac, String apMac, List<String> shares) {
		this.id = id;
		this.type = type;
		this.mode = mode;
		this.user = user;
		this.deviceMac = deviceMac;
		APMac = apMac;
		this.shares = shares;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getDeviceMac() {
		return deviceMac;
	}
	public void setDeviceMac(String deviceMac) {
		this.deviceMac = deviceMac;
	}
	public List<String> getShares() {
		return shares;
	}
	public void setShares(List<String> shares) {
		this.shares = shares;
	}

	public String getAPMac() {
		return APMac;
	}

	public void setAPMac(String APMac) {
		this.APMac = APMac;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}

