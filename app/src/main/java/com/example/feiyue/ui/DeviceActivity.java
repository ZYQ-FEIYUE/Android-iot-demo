package com.example.feiyue.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.feiyue.FeiYueApp;
import com.example.feiyue.R;
import com.example.feiyue.bean.DeviceUpload;
import com.example.feiyue.bean.RgbBroad;
import com.example.feiyue.connect.MqttUtil;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DeviceActivity extends AppCompatActivity {
    private static final String TAG = "DeviceActivity";
    @BindView(R.id.colorPickerView)
    ColorPickerView colorPickerView;
    @BindView(R.id.RGB_text)
    TextView RGBText;
    public static final String DEVICE_MAC = "device_mac";
    @BindView(R.id.back_btn_image)
    ImageButton backBtnIV;
    @BindView(R.id.back_btn)
    Button backBtn;
    @BindView(R.id.rgb_red_btn)
    Button rgbRedBtn;
    @BindView(R.id.rgb_green_btn)
    Button rgbGreenBtn;
    @BindView(R.id.rgb_blue_btn)
    Button rgbBlueBtn;
    @BindView(R.id.rgb_close_btn)
    Button rgbCloseBtn;
    @BindView(R.id.rgb_open_btn)
    Button rgbOpenBtn;
    private String deviceBssid;
    private RgbBroad rgbBroad;
    private String pubTopic;        //发布topic
    private WifiManager mWifiManager;
    //http,回传
    private okhttp3.Callback mHttpCallback;
    //线程消息
    private boolean mLocalNetWork = false;      //是否处于局域网标志位
    private boolean mMQTTConnectDevice = false;     //判断mqtt方式是否连接上设备
    private static final int MSG_WIFI_DISCONNECT = 1;   //wifi未连接消息
    private static final int MSG_WIFI_CONNECT = 2;      //wifi连接消息
    private static final int MSG_DEVICE_CALLBACK = 3;   //设备状态回调
    private String deviceHttpUrl;
    private DeviceUpload deviceUpload;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        deviceBssid = intent.getStringExtra(DEVICE_MAC);//设备mac
        pubTopic = MqttUtil.PUB_TITLE + deviceBssid;
        //实例化控制
        rgbBroad = new RgbBroad();
        //拾色器
        final BrightnessSlideBar brightnessSlideBar = findViewById(R.id.brightnessSlide);
        colorPickerView.attachBrightnessSlider(brightnessSlideBar);
        colorPickerView.setLifecycleOwner(this);
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                RGBText.setText("#" + envelope.getHexCode());
                int[] rgb_get = envelope.getArgb();
                int[] rgb_send = new int[3];
                System.arraycopy(rgb_get, 1, rgb_send, 0, 3);
                if (mLocalNetWork) {        //处于局域网下
                    rgbBroad.rgbPWMHttp(deviceHttpUrl, rgb_send, mHttpCallback);
                } else {
                    rgbBroad.rgbPWMMqtt(pubTopic, rgb_send);
                }
            }
        });
        MqttUtil.getInstance(this).setMqttReceivedCallback(new MqttUtil.OnMqttReceivedCallbackBlock() {
            @Override
            public void callBack(String topic, String jsonString) {
                Gson gson = new Gson();
                deviceUpload = gson.fromJson(jsonString, DeviceUpload.class);
                assert deviceBssid != null;
                if (deviceBssid.equals(deviceUpload.getDeviceMac())) {
                    mMQTTConnectDevice = true;      //mqtt连接上设备
                    int[] deviceIP = deviceUpload.getDeviceIp();    //解析设备ip
                    deviceHttpUrl = (deviceIP[0] & 0xff) + "." + (deviceIP[1] & 0xff)  + "." + (deviceIP[2] & 0xff)  + "." + (deviceIP[3] & 0xff);
                    mHandler.removeMessages(MSG_WIFI_CONNECT);
                    mHandler.sendEmptyMessageDelayed(MSG_WIFI_CONNECT, 10);
                }
            }
        });
        //检查wifi变化
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        FeiYueApp.getInstance().observeBroadcast(DeviceActivity.this, broadcast -> {
            Log.d(TAG, "onCreate: Broadcast=" + broadcast);
            onWifiChanged();
        });
        //设置http回调
        mHttpCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mLocalNetWork = false;
                Log.i("Http", "fail");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    assert response.body() != null;
                    String result = response.body().string();
                    Log.i(TAG, result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String getResult = jsonObject.getString("value");
                        if (getResult != null) {
                            mLocalNetWork = true;       //设备处于局域网
                            Log.i("Http", "success");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        showProgressDialog();
//        colorPickerView.setActionMode(ActionMode.ALWAYS);
//        colorPickerView.setDebounceDuration(150);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.removeMessages(MSG_DEVICE_CALLBACK);         //返回设备状态
        mHandler.sendEmptyMessageDelayed(MSG_DEVICE_CALLBACK, 500);
        //
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMQTTConnectDevice = false;     //连接标志位
    }

    private void onWifiChanged() {
        WifiInfo info = mWifiManager.getConnectionInfo();
        boolean disconnected = info == null || info.getIpAddress() == 0;
        if (disconnected) {     //没有连接上wifi,因网络波动会频繁调用，所以500ms后处理
            mHandler.removeMessages(MSG_WIFI_DISCONNECT);
            mHandler.sendEmptyMessageDelayed(MSG_WIFI_DISCONNECT, 500);
            mHandler.removeMessages(MSG_DEVICE_CALLBACK);         //返回设备状态
            mHandler.sendEmptyMessageDelayed(MSG_DEVICE_CALLBACK, 500);
            Log.i(TAG, "未连接上wifi");
        } else {        //连接上网络再判断是否处于局域网下
            mHandler.removeMessages(MSG_WIFI_CONNECT);
            mHandler.sendEmptyMessageDelayed(MSG_WIFI_CONNECT, 1000);
            mHandler.removeMessages(MSG_DEVICE_CALLBACK);         //返回设备状态
            mHandler.sendEmptyMessageDelayed(MSG_DEVICE_CALLBACK, 500);
            Log.i(TAG, "连接上wifi");
        }
    }
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_WIFI_CONNECT:      //连接上wifi,判断是否与设备处于局域网下
                    if (mMQTTConnectDevice) {       //mqtt通讯成功，获取到设备ip，使用http ping 设备
                        rgbBroad.rgbQueryMessageHttp(deviceHttpUrl, mHttpCallback);
                    } else {
                        mHandler.removeMessages(MSG_WIFI_CONNECT);
                        mHandler.sendEmptyMessageDelayed(MSG_WIFI_CONNECT, 500);
                    }
                    break;
                case MSG_WIFI_DISCONNECT:   //未连接上wifi
                    showProgressDialog();
                    mLocalNetWork = false;
                    break;
                case MSG_DEVICE_CALLBACK:
                    if (MqttUtil.getInstance(getApplicationContext()).isConnectSuccess) {       //mqtt连上后才发送
                        rgbBroad.rgbQueryMessageMqtt(pubTopic);
                        closeProgressDialog();
                    } else {
                        mHandler.removeMessages(MSG_DEVICE_CALLBACK);         //500ms后继续判断
                        mHandler.sendEmptyMessageDelayed(MSG_DEVICE_CALLBACK, 500);
                    }
                    break;
            }
            return true;
        }
    });
    @OnClick({R.id.back_btn_image, R.id.back_btn, R.id.rgb_red_btn, R.id.rgb_green_btn, R.id.rgb_blue_btn, R.id.rgb_close_btn, R.id.rgb_open_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn_image:
                finish();
                break;
            case R.id.back_btn:
                showAboutDialog();
                break;
            case R.id.rgb_red_btn:  //红灯
                int[] pwm_red = {255, 0, 0};
                if (mLocalNetWork) {        //处于局域网下
                    rgbBroad.rgbPWMHttp(deviceHttpUrl, pwm_red, mHttpCallback);
                } else {
                    rgbBroad.rgbPWMMqtt(pubTopic, pwm_red);
                }
                break;
            case R.id.rgb_green_btn:
                int[] pwm_green = {0, 255, 0};
                if (mLocalNetWork) {        //处于局域网下
                    rgbBroad.rgbPWMHttp(deviceHttpUrl, pwm_green, mHttpCallback);
                } else {
                    rgbBroad.rgbPWMMqtt(pubTopic, pwm_green);
                }
                break;
            case R.id.rgb_blue_btn:
                int[] pwm_blue = {0, 0, 255};
                if (mLocalNetWork) {        //处于局域网下
                    rgbBroad.rgbPWMHttp(deviceHttpUrl, pwm_blue, mHttpCallback);
                } else {
                    rgbBroad.rgbPWMMqtt(pubTopic, pwm_blue);
                }
                break;
            case R.id.rgb_close_btn:
                if (mLocalNetWork) {        //处于局域网下
                    rgbBroad.rgbPowerHttp(deviceHttpUrl, false, mHttpCallback);
                } else {
                    rgbBroad.rgbPowerMqtt(pubTopic, false);
                }
                break;
            case R.id.rgb_open_btn:
                if (mLocalNetWork) {        //处于局域网下
                    rgbBroad.rgbPowerHttp(deviceHttpUrl, true, mHttpCallback);
                } else {
                    rgbBroad.rgbPowerMqtt(pubTopic, true);
                }
                break;
        }
    }
    private void showAboutDialog() {
        CharSequence[] items = new CharSequence[]{};
        if (deviceUpload != null) {
            String mode = mLocalNetWork ? "局域网": "远程连接";
            items = new CharSequence[]{
                    "当前模式：" + mode,
                    "设备IP：" + deviceHttpUrl,
                    "设备MAC：" + deviceBssid
            };
        }

        new AlertDialog.Builder(this)
                .setTitle("当前状态")
                .setIcon(R.drawable.baseline_info_black_24)
                .setItems(items, null)
                .show();
    }
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在连接...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
