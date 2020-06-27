package com.example.feiyue.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.speech.asr.SpeechConstant;
import com.example.feiyue.MainActivity;
import com.example.feiyue.R;
import com.example.feiyue.bean.Device;
import com.example.feiyue.bean.Person;
import com.example.feiyue.bean.RgbBroad;
import com.example.feiyue.connect.MqttUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_FINISHED;

public class AsrActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 0x01;
    @BindView(R.id.title_text)
    TextView titleTV;
    @BindView(R.id.back_btn_image)
    ImageButton backBtnIV;
    @BindView(R.id.back_btn)
    Button backBtn;
    @BindView(R.id.voice_image)
    FloatingActionButton voiceFab;
    @BindView(R.id.asr_result)
    TextView asrResultTV;
    @BindView(R.id.rec_text)
    TextView recTV;
    @BindView(R.id.tip1_text)
    TextView tip1TV;
    @BindView(R.id.tip2_text)
    TextView tip2TV;
    private MyRecognizer myRecognizer;
    private Handler handler;
    String regEx = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";
    String aa = "";
    private List<String[]> listString = new ArrayList<>();
    private String[] light_red = {"彩灯红色", "灯红色", "七彩灯设为红色", "红色"};
    private String[] light_blue = {"彩灯蓝色", "灯蓝色", "七彩灯设为蓝色", "蓝色"};
    private String[] light_green = {"彩灯绿色", "灯绿色", "七彩灯设为绿色", "绿色"};
    private String[] light_open = {"打开彩灯", "打开灯", "打开台灯", "打开七彩灯", "把彩灯打开", "帮我打开彩灯吧", "彩灯打开"};
    private String[] light_close = {"关闭彩灯", "关闭灯", "关闭台灯", "关闭七彩灯", "彩灯关闭", "灯关闭"};
    private Device mDevice;
    private RgbBroad rgbBroad;
    private String pubTopic;
    private boolean recResult = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asr);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String personStr = intent.getStringExtra(MainActivity.TRANS_PERSON);
        Gson gson = new Gson();
        Person person = gson.fromJson(personStr, Person.class);
        mDevice = getDevice(person);
        titleTV.setText("语音控制");
        rgbBroad = new RgbBroad();
        if (mDevice != null) {
            pubTopic = MqttUtil.PUB_TITLE + mDevice.getDeviceMac();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {       //动态申请权限
            String[] permissions = {Manifest.permission.RECORD_AUDIO};
            requestPermissions(permissions, REQUEST_PERMISSION);
        }
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                handleMsg(msg);
                return true;
            }
        });
        initSpeechRec();    //先初始化才初始化语音
        startSpeechRec();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "授权失败，即将退出程序.....", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private Device getDevice(Person person) {
        List<Device> devices = person.getDevices();
        if (devices != null) {
            int index = -1;
            for (Device device : devices) {
                if (device.getType().equals("rgb")) {
                    index = devices.indexOf(device);
                    break;
                }
            }
            if (index != -1)
                return devices.get(index);
            else
                return null;
        } else {
            return null;
        }
    }
    //初始化语音识别
    private void initSpeechRec() {
        IRecogListener listener = new MessageStatusRecogListener(handler);
        if (myRecognizer == null) {
            myRecognizer = new MyRecognizer(this, listener);
        }
    }

    //开始识别
    private void startSpeechRec() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.PID, 1537);   //普通话
        myRecognizer.start(params);
    }

    //
    private void stopSpeechRec() {
        myRecognizer.stop();
    }

    //回调事件回调
    private void handleMsg(Message msg) {
        if (msg.what == STATUS_FINISHED) {
            JSONObject msgObj = null;
            String bestResult = null;
            try {
                msgObj = new JSONObject(msg.obj.toString());
                bestResult = msgObj.getString("best_result");
                bestResult = bestResult.replaceAll(regEx, "");
                handleAsr(bestResult);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ColorStateList colorStateList = ContextCompat.getColorStateList(getApplicationContext(), R.color.color_C64D4D4D);
            voiceFab.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
            voiceFab.setBackgroundTintList(colorStateList);
            voiceFab.setImageResource(R.drawable.mi_brain_float_view_press);
            if (!recResult) {
                asrResultTV.setText("对不起，我没听懂");
            }
            tip1TV.setVisibility(View.INVISIBLE);
            tip2TV.setVisibility(View.INVISIBLE);
            recTV.setText("");
        }
    }

    private void handleAsr(String result) {
        listString.add(light_red);
        listString.add(light_green);
        listString.add(light_blue);
        listString.add(light_open);
        listString.add(light_close);
        int flag = 0;
        for (String[] s : listString) {
            for (String s1 : s) {
                if (result.contains(s1)) {
                    flag = listString.indexOf(s) + 1;
                    break;
                }
            }
            if (flag > 0) {
                asrResultTV.setText(result);
                recResult = true;
                break;
            }
        }
        switch (flag) {
            case 1:
                if (mDevice != null) {
                    int[] pwm_red = {255, 0, 0};
                    rgbBroad.rgbPWMMqtt(pubTopic, pwm_red);
                }
                break;
            case 2:
                if (mDevice != null) {
                    int[] pwm_green = {0, 255, 0};
                    rgbBroad.rgbPWMMqtt(pubTopic, pwm_green);
                }
                break;
            case 3:
                if (mDevice != null) {
                    int[] pwm_blue = {0, 0, 255};
                    rgbBroad.rgbPWMMqtt(pubTopic, pwm_blue);
                }
                break;
            case 4:
                if (mDevice != null) {
                    rgbBroad.rgbPowerMqtt(pubTopic, true);
                }
                break;
            case 5:
                if (mDevice != null) {
                    rgbBroad.rgbPowerMqtt(pubTopic, false);
                }
                break;
            default:
                asrResultTV.setText("对不起，我没听懂");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myRecognizer != null) {
            myRecognizer.release();
            myRecognizer = null;
        }
    }

    @OnClick({R.id.back_btn_image, R.id.back_btn, R.id.voice_image})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn_image:
            case R.id.back_btn:
                finish();
                break;
            case R.id.voice_image:
                ColorStateList colorStateList = ContextCompat.getColorStateList(getApplicationContext(), R.color.colorAccent);
                voiceFab.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
                voiceFab.setBackgroundTintList(colorStateList);
                voiceFab.setImageResource(R.drawable.mi_brain_float_view);
                startSpeechRec();
                tip1TV.setVisibility(View.VISIBLE);
                tip2TV.setVisibility(View.VISIBLE);
                recTV.setText("识别中");
                asrResultTV.setText("");
                recResult = false;
                break;
        }
    }
}
