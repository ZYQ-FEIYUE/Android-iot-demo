package com.example.feiyue.bean;

import android.content.Context;
import android.util.Log;

import com.example.feiyue.connect.HttpUpdate;
import com.example.feiyue.connect.HttpUtil;
import com.example.feiyue.connect.MqttUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//pwm
//{
//    "change": "pwm",
//    "value": [255, 0, 0]
//}
//开关灯
//{
//    "change": "power",
//    "value": "false"
//}
public class RgbBroad {
    private Context context;
    //控制设备电源
    public void rgbPowerMqtt(String topic, boolean status) {
        Map<Object, Object> map = new HashMap<>();
        String power = status ? "true" : "false";
        map.put("change", "power");      //控制开关灯
        map.put("value", power);
        Gson gson = new Gson();
        String param= gson.toJson(map);
        MqttUtil.getInstance(context).publish(topic, param);
    }

    public void rgbPWMMqtt(String topic, int[] pwm) {
        Map<Object, Object> map = new HashMap<>();
        map.put("change", "pwm");      //控制开关灯
        map.put("value", pwm);
        Gson gson = new Gson();
        String param= gson.toJson(map);
        MqttUtil.getInstance(context).publish(topic, param);
    }
    public void rgbQueryMessageMqtt(String topic) {
        Map<Object, Object> map = new HashMap<>();
        map.put("change", "query");      //控制开关灯
        map.put("value", "true");
        Gson gson = new Gson();
        String param= gson.toJson(map);
        Log.i("tag", param);
        MqttUtil.getInstance(context).publish(topic, param);
    }
    public void rgbPowerHttp(String url, boolean status, okhttp3.Callback callback) {
        assert url != null;
        String path = "http://" + url + "/adder";
        Map<Object, Object> map = new HashMap<>();
        String power = status ? "true" : "false";
        map.put("change", "power");      //控制开关灯
        map.put("value", power);
        HttpUtil.okHttpPostJson(path, map, callback);
    }
    public void rgbPWMHttp(String url, int[] pwm, okhttp3.Callback callback) {
        assert url != null;
        String path = "http://" + url + "/adder";
        Map<Object, Object> map = new HashMap<>();
        map.put("change", "pwm");      //控制开关灯
        map.put("value", pwm);
        HttpUtil.okHttpPostJson(path, map, callback);
    }
    public void rgbQueryMessageHttp(String url, okhttp3.Callback callback) {
        assert url != null;
        String path = "http://" + url + "/adder";
        Map<Object, Object> map = new HashMap<>();
        map.put("change", "http_query");      //控制开关灯
        map.put("value", "true");
        HttpUtil.okHttpPostJson(path, map, callback);
    }
}
