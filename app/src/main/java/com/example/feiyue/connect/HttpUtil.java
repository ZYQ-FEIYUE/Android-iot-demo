package com.example.feiyue.connect;


import android.util.Log;

import com.example.feiyue.bean.Person;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
//            .connectTimeout(50L, TimeUnit.SECONDS)
//                        .readTimeout(60L, TimeUnit.SECONDS)
//                        .build();
    public static void okHttpGet(String url, okhttp3.Callback callback) {
//        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Connection", "close")
                .build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }
    public static void okHttpGetAPP(String url, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public static void okHttpWait(String url, okhttp3.Callback callback) {
//        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Connection", "close")
                .build();
        mOkHttpClient.newCall(request).enqueue(callback);
        synchronized (Thread.currentThread()) {
            try {
                Thread.currentThread().wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //post device json
    public static void okHttpPostJson(String url, Map<Object, Object> map, okhttp3.Callback callback) {
//        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        String param= gson.toJson(map);
        RequestBody requestBody = RequestBody.create(JSON, param);
//        FormBody body = new FormBody.Builder()
//                .add("change", "http_query")
//                .add("value","true")
//                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .addHeader("Connection", "close")
                .post(requestBody)
                .build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

}
