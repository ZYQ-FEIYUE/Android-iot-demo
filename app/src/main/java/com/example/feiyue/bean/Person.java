package com.example.feiyue.bean;

import java.util.List;

public class Person {
	private boolean status;		//连接状态
	private String name;		//用户名
	private List<Device> devices;	//用户设备

	public List<Device> getDevices() {
		return devices;
	}
	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}
	public boolean getStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
