package com.example.feiyue.connect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.feiyue.bean.DeviceIdUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MqttUtil {
    private final String TAG = "------------->mqtt";
    @SuppressLint("StaticFieldLeak")
    private static MqttUtil mqttUtil;
    private Context context;

    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;
    public boolean isConnectSuccess = false;
    //MQTT相关配置
    private String CLIENTID;
    private String HOST = "tcp://rau32dc.mqtt.iot.gz.baidubce.com:1883";//服务器地址（协议+地址+端口号）
    private String USERNAME = "rau32dc/android";//用户名
    private String PASSWORD = "UkfO2sZs5rUKZrG4";//密码
    public static String[] SUB_TOPIC = new String[20];
    public static String PUBLISH_TOPIC = "device/a";//发布主题
    public static String RESPONSE_TOPIC = "theme/city_id/country_id/company_id/sn";//订阅主题
    public final static String SUB_TITLE = "AP/";
    public final static String PUB_TITLE = "device/";
//    private final String SUB_TOPIC = "phone/a";
    /**
     * QUALITY_OF_SERVICE
     * 至多一次，消息发布完全依赖底层 TCP/IP 网络。会发生消息丢失或重复。这一级别可用于如下情况，环境传感器数据，丢失一次读记录无所谓，因为不久后还会有第二次发送。
     * 至少一次，确保消息到达，但消息重复可能会发生。
     * 只有一次，确保消息到达一次。这一级别可用于如下情况，在计费系统中，消息重复或丢失会导致不正确的结果
     */
    private final int[] QUALITY_OF_SUB = {1};//服务质量,0最多一次，1最少一次，2只一次
    private final int QUALITY_OF_PUB = 0;//服务质量,0最多一次，1最少一次，2只一次

    private final int HAND_RECONNECT = 1;//重连hand
    private final int RECONNECT_TIME_CONFIG = 10 * 1000;//重连时间间隔为10秒
    private OnMqttReceivedCallbackBlock receivedCallback;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HAND_RECONNECT:
                    if (!isConnectSuccess)
                        doClientConnection();//连接失败，重连（可关闭服务器进行模拟）
                    break;
            }
        }
    };

    //MQTT是否连接成功的监听
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            isConnectSuccess = true;
            try {
                if (SUB_TOPIC != null) {
                    for (String s : SUB_TOPIC) {
                        mqttAndroidClient.subscribe(s, 1);//订阅主题，参数：主题、服务质量
                    }
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Log.i(TAG, "onFailure 连接失败:" + arg1.getMessage());
            isConnectSuccess = false;
            handler.sendEmptyMessageDelayed(HAND_RECONNECT, RECONNECT_TIME_CONFIG);
        }
    };

    //订阅主题的回调
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, topic + "收到消息： " + new String(message.getPayload()) + "\tToString:" + message.toString());
            //收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等
            //response("message arrived:"+message);
            if (receivedCallback != null) {
                receivedCallback.callBack(topic, new String(message.getPayload()));
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            Log.i(TAG, "deliveryComplete");
        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.i(TAG, "连接断开");
            doClientConnection();//连接断开，重连
        }
    };

    //单例模式
    public static MqttUtil getInstance(Context context) {
        if (mqttUtil == null) {
            mqttUtil = new MqttUtil(context);
        }
        return mqttUtil;
    }
    /**
     * 释放单例
     */
    public static void release() {
        if (mqttUtil != null) {
            mqttUtil.disconnect();
            mqttUtil = null;
        }
    }
    private MqttUtil(Context context) {
        this.context = context;
        initMqtt();
    }

    @SuppressLint("MissingPermission")
    private void initMqtt() {
        String serverURI = HOST; //服务器地址（协议+地址+端口号）
        CLIENTID = DeviceIdUtils.getDeviceId(context);
        mqttAndroidClient = new MqttAndroidClient(context, serverURI, CLIENTID);
        mqttAndroidClient.setCallback(mqttCallback); //设置订阅消息的回调
        mMqttConnectOptions = new MqttConnectOptions();
        mMqttConnectOptions.setCleanSession(true); //设置是否清除缓存
        mMqttConnectOptions.setConnectionTimeout(10); //设置超时时间，单位：秒
        mMqttConnectOptions.setKeepAliveInterval(300); //设置心跳包发送间隔，单位：秒
        mMqttConnectOptions.setUserName(USERNAME); //设置用户名
        mMqttConnectOptions.setPassword(PASSWORD.toCharArray()); //设置密码

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + CLIENTID + "\"}";
        String topic = PUBLISH_TOPIC;
        if ((!message.equals("")) || (!topic.equals(""))) {
            try {
                mMqttConnectOptions.setWill(topic, message.getBytes(), QUALITY_OF_PUB, false);
            } catch (Exception e) {
                Log.i(TAG, "setWill Exception Occured:" + e.getMessage(), e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }
        if (doConnect) {
            Log.i(TAG,"mMqttConnectOptions.setWill Success");
            doClientConnection();
        }
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        Log.i(TAG,"是否链接成功：" + mqttAndroidClient.isConnected());
        if (!mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                Log.i(TAG, "doClientConnection:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 发布消息
     *
     * @param message 消息
     */
    public void publish(String publishTopic, String message) {
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
                mqttAndroidClient.publish(publishTopic, message.getBytes(), QUALITY_OF_PUB, false);
            } else {
                Log.i(TAG, "mqttAndroidClient is Null");
            }
        } catch (MqttException e) {
            Log.i(TAG,"publish MqttException:" + e.getMessage());
            e.printStackTrace();
        }
    }
    public void unSubscribe(String topic) {
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.unsubscribe(topic);
            } else {
                Log.i(TAG, "mqttAndroidClient is Null");
            }
        } catch (MqttException e) {
            Log.i(TAG,"unsubscribe MqttException:" + e.getMessage());
            e.printStackTrace();
        }
    }
    public void subscribe(String topic) {
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.subscribe(topic, 1);
                Log.i(TAG, "mqttAndroidClient subscribe");
            } else {
                Log.i(TAG, "mqttAndroidClient is Null");
            }
        } catch (MqttException e) {
            Log.i(TAG,"unsubscribe MqttException:" + e.getMessage());
            e.printStackTrace();
        }
    }
    public void response(String message) {
        String topic = RESPONSE_TOPIC;
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient.publish(topic, message.getBytes(), QUALITY_OF_PUB, false);
        } catch (MqttException e) {
            Log.i(TAG,"publish:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //断开链接
    public void disconnect() {
        try {
            if (mqttAndroidClient != null)
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
                mqttAndroidClient.disconnect();
                mqttAndroidClient = null;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public interface OnMqttReceivedCallbackBlock {
        void callBack(String topic, String jsonString);
    }
    public void setMqttReceivedCallback(OnMqttReceivedCallbackBlock receivedCallback) {
        this.receivedCallback = receivedCallback;
    }
}
