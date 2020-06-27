package com.example.feiyue.connect;

import android.text.TextUtils;
import android.util.Log;

import com.example.feiyue.bean.Device;
import com.example.feiyue.bean.Person;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class  HttpUpdate {
    private static boolean flag = false;
    private static final Gson gson = new Gson();
    private static Person person;
    //更新数据
    public static boolean updateUserPerson(String url, String email, String person) {
//        "select * from member where email = '"+email+"' and password = '"+password+"'";
//        "http://192.168.31.75:8081/AndroidTest/Login?email="+email+"&password="+password;
        String temp = "null";
        try {
            temp = URLEncoder.encode(person, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String path = "http://" + url + ":8080/AndroidTest/DeviceMessage?email=" + email + "&person="+temp;
        HttpUtil.okHttpWait(path, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                flag = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    assert response.body() != null;
                    String result = response.body().string();
                    Person person = gson.fromJson(result, Person.class);
                    flag = person.getStatus();
                }
            }
        });
        return flag;
    }
    //查询数据
    public static Person queryUserPerson(String url, String email) {

        String path = "http://" + url + ":8080/AndroidTest/DeviceQuery?email=" + email;
        HttpUtil.okHttpWait(path, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                person = new Person();
                person.setStatus(false);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    assert response.body() != null;
                    String result = response.body().string();
                    person = gson.fromJson(result, Person.class);
                    Log.i("TAG", String.valueOf(person.getStatus()));
                } else {
                    person = new Person();
                    person.setStatus(false);
                }
            }
        });
        return person;
    }
    public static void queryPersonFromServer(String url, String email, OnHttpReceivedCallbackBlock receivedCallback) {
        String path = "http://" + url + ":8080/AndroidTest/DeviceQuery?email=" + email;

        HttpUtil.okHttpGet(path, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                receivedCallback.failCallBack();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    assert response.body() != null;
                    String result = response.body().string();
                    Person person = gson.fromJson(result, Person.class);
                    receivedCallback.callBack(person);
                    Log.i("HTTPClient", String.valueOf(person.getStatus()));
                }
            }
        });
    }
    public static void updatePersonToServer(String url, String email, String personJson, OnHttpReceivedCallbackBlock receivedCallback) {
        String temp = "null";
        try {
            temp = URLEncoder.encode(personJson, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String path = "http://" + url + ":8080/AndroidTest/DeviceMessage?email=" + email + "&person="+temp;
        HttpUtil.okHttpGet(path, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                receivedCallback.failCallBack();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    assert response.body() != null;
                    String result = response.body().string();
                    Person person = gson.fromJson(result, Person.class);
                    receivedCallback.callBack(person);
                    Log.i("HTTPClient", String.valueOf(person.getStatus()));
                }
            }
        });
    }
    public interface OnHttpReceivedCallbackBlock {
        void callBack(Person person);
        void failCallBack();
    }
//    public void setMqttReceivedCallback(OnHttpReceivedCallbackBlock receivedCallback) {
//        this.receivedCallback = receivedCallback;
//    }
}
