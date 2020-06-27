package com.example.feiyue.handle;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.feiyue.MainActivity;
import com.example.feiyue.R;
import com.example.feiyue.bean.Common;
import com.example.feiyue.bean.Device;
import com.example.feiyue.bean.Login;
import com.example.feiyue.bean.Person;
import com.example.feiyue.connect.HttpUpdate;
import com.example.feiyue.connect.HttpUtil;
import com.example.feiyue.dialog.ASlideDialog;
import com.example.feiyue.ui.LoginActivity;
import com.google.gson.Gson;

import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DeviceEvent {
    private Activity mActivity;
    public DeviceEvent(Activity activity) {
        mActivity = activity;
    }
    public void deleteDevice(Person userPerson, int position, OnDeleteCallback deleteCallback) {
        List<Device> userDevices = userPerson.getDevices();
        //如果是分享设备则需要删除原主人的设备
        if (userDevices.get(position).getMode().equals("share")) {
            String firstUser = userDevices.get(position).getUser();
            String deviceMac = userDevices.get(position).getDeviceMac();
            HttpUpdate.queryPersonFromServer(Common.url, firstUser, new HttpUpdate.OnHttpReceivedCallbackBlock() {
                @Override
                public void callBack(Person person) {
                    List<Device> devices = person.getDevices();
                    for (Device device : devices) {
                        //从分享列表移除
                        if (device.getUser().equals(firstUser) && device.getDeviceMac().equals(deviceMac)) {
                           device.getShares().remove(Common.userEmail);
                           break;
                        }
                    }
                    //更新
                    Gson gson = new Gson();
                    String json = gson.toJson(person, Person.class);
                    HttpUpdate.updatePersonToServer(Common.url, firstUser, json, new HttpUpdate.OnHttpReceivedCallbackBlock() {
                        @Override
                        public void callBack(Person person) {

                        }

                        @Override
                        public void failCallBack() {

                        }
                    });
                }

                @Override
                public void failCallBack() {

                }
            });
        }
        userDevices.remove(position);       //删除设备
        Gson gson = new Gson();
        String jsonPerson = gson.toJson(userPerson, Person.class);  //转为json字符串
        HttpUpdate.updatePersonToServer(Common.url, Common.userEmail, jsonPerson, new HttpUpdate.OnHttpReceivedCallbackBlock() {
            @Override
            public void callBack(Person person) {
                deleteCallback.CallBack();
            }

            @Override
            public void failCallBack() {

            }
        });
    }
    public void shareDevice(Person userPerson, int position) {
        final Dialog dialog = ASlideDialog.newInstance(mActivity, ASlideDialog.Gravity.Center, R.layout.share_device_dialog);
        final EditText editText = dialog.findViewById(R.id.share_device_email_edittext);
        dialog.findViewById(R.id.share_device_email_ok_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editText.getText().toString();
                if (!"".equals(email)) {
                    //获取对方的person对象
                    if (email.equals(Common.userEmail)) {
                        Toast.makeText(mActivity.getApplicationContext(), "不能共享给自己", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (userPerson.getDevices().get(position).getShares().contains(email)) {
                        Toast.makeText(mActivity.getApplicationContext(), "该用户已经共享", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //对方邮箱person对象
                    HttpUpdate.queryPersonFromServer(Common.url, email, new HttpUpdate.OnHttpReceivedCallbackBlock() {
                        @Override
                        public void callBack(Person person) {
                            if (person.getStatus()) {       //查询成功
                                //创建设备
                                Device userDevice = userPerson.getDevices().get(position);
                                List<String> shares = new ArrayList<>();
                                userDevice.setMode("share");        //设置为共享模式
                                shares = userDevice.getShares();
                                userDevice.setShares(new ArrayList<>());
                                List<Device> devices;
                                if (person.getDevices() != null) {       //device不为空
                                    devices = person.getDevices();
                                } else {    //用户设备为空
                                    devices = new ArrayList<>();
                                }
                                devices.add(userDevice);        //增加设备
                                person.setDevices(devices); //更新设备
                                Gson gson = new Gson();     //上传服务器
                                String jsonPerson = gson.toJson(person, Person.class);  //转为json字符串
                                HttpUpdate.updatePersonToServer(Common.url, email, jsonPerson, new HttpUpdate.OnHttpReceivedCallbackBlock() {
                                    @Override
                                    public void callBack(Person person) {
                                    }
                                    @Override
                                    public void failCallBack() {
                                    }
                                });
                                //更新user数据
                                List<Device> userDevices = userPerson.getDevices(); //得到所有设备
                                userDevice.setMode("my");   //设置为原来参数
//                                List<String> userShares = userDevice.getShares();   //增加分享用户
                                if (!shares.contains(email)) {
                                    shares.add(email);
                                    userDevice.setShares(shares);       //更新
                                }
                                userDevices.set(position, userDevice);  //更新设备列表信息
                                userPerson.setDevices(userDevices);     //更新
                                String userPersonJson = gson.toJson(userPerson, Person.class);  //转为json字符串
                                HttpUpdate.updatePersonToServer(Common.url, Common.userEmail, userPersonJson, new HttpUpdate.OnHttpReceivedCallbackBlock() {
                                    @Override
                                    public void callBack(Person person) {
                                        mActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.dismiss();
                                                Toast.makeText(mActivity.getApplicationContext(), "分享成功", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    @Override
                                    public void failCallBack() {

                                    }
                                });
                            } else {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mActivity.getApplicationContext(), "对方邮箱不存在", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void failCallBack() {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity.getApplicationContext(), "分享失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(mActivity.getApplicationContext(), "信息输入不完整", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.findViewById(R.id.share_device_email_cancel_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
    public void reviseName() {
        final Dialog dialog = ASlideDialog.newInstance(mActivity, ASlideDialog.Gravity.Center, R.layout.revise_dialog);
        final EditText editText = dialog.findViewById(R.id.revise_name_edittext);
        dialog.findViewById(R.id.revise_ok_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.revise_cancel_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
    public void showAPPInfo() {
        String appVer = "";
        PackageManager packageManager = mActivity.getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(mActivity.getPackageName(), 0);
            appVer = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final Dialog dialog = ASlideDialog.newInstance(mActivity, ASlideDialog.Gravity.Center, R.layout.show_app_info_dialog);
        final TextView textView = dialog.findViewById(R.id.ver_text);
        textView.setText("APP版本：" + appVer);
        dialog.show();
    }
    public void showLogOutDialog() {
        final Dialog dialog = ASlideDialog.newInstance(mActivity, ASlideDialog.Gravity.Center, R.layout.log_out_dialog);
        dialog.findViewById(R.id.log_out__ok_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login login = new Login(mActivity);
                login.setIsLogin(false);
                dialog.dismiss();
                Intent intent = new Intent(mActivity, LoginActivity.class);
                mActivity.startActivity(intent);
                mActivity.finish();
            }
        });
        dialog.findViewById(R.id.log_out_cancel_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
    public interface OnDeleteCallback {
        public void CallBack();
    }
}
