package com.example.feiyue;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.feiyue.adapter.LinkDevice;
import com.example.feiyue.adapter.LinkDeviceAdapter;
import com.example.feiyue.bean.AppUtils;
import com.example.feiyue.bean.Common;
import com.example.feiyue.bean.Device;
import com.example.feiyue.bean.Login;
import com.example.feiyue.bean.Person;
import com.example.feiyue.bean.Version;
import com.example.feiyue.connect.HttpUpdate;
import com.example.feiyue.connect.HttpUtil;
import com.example.feiyue.connect.MqttUtil;
import com.example.feiyue.connect.MyMqttService;
import com.example.feiyue.handle.DeviceEvent;
import com.example.feiyue.ui.AsrActivity;
import com.example.feiyue.ui.EspTouchActivity;
import com.example.feiyue.ui.ShareActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import constant.UiType;
import listener.Md5CheckResultListener;
import listener.UpdateDownloadListener;
import model.UiConfig;
import model.UpdateConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import update.UpdateAppUtils;

public class MainActivity extends AppCompatActivity implements MotionLayout.TransitionListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    @BindView(R.id.motionLayout)
    MotionLayout motionLayout;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_open)
    ImageView navIV;
    @BindView(R.id.add_image)
    ImageView addIV;
    @BindView(R.id.home_text)
    TextView homeTV;
    @BindView(R.id.add_image2)
    ImageView addIV2;
    @BindView(R.id.image2_layout)
    CoordinatorLayout image2Layout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.device_title_count)
    TextView deviceTitleTV;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private float lastProgress = 0f;        //motion进程
    private RefreshLayout refreshLayout;
    private Intent intentService;           //服务
    private LinkDeviceAdapter mLinkAdapter;       //设备列表
    private List<LinkDevice> mLinkDeviceList = new ArrayList<>();
    private Person userPerson;
    private DeviceEvent deviceEvent;
    public static final String TRANS_PERSON = "trans_person";
    private String apkUrl = "http://zyqfeiyue.gitee.io/test_pages/app/feiyue-release.apk";
    private String updateTitle = "发现新版本";
    private String updateContent = "1、测试版本更新功能\n2、更多功能等你探索";
    private final int MSG_VERSION_UPDATE = 1;
    private final int MSG_VERSION_LATEST = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        motionLayout.setTransitionListener(MainActivity.this);
        navView.setNavigationItemSelectedListener(this);
        //获取用户昵称
        Login login = new Login(this);
        homeTV.setText(login.getName() + "的家 >");        //home标题

        //下拉刷新
        refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new MaterialHeader(this));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh(2000);//传入false表示刷新失败
                refreshLinkDevice();
            }
        });
        //初始化设备列表
//        initLinkDevice();
        intentService = new Intent(this, MyMqttService.class);      //开启mqtt服务
        startService(intentService);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        mLinkAdapter = new LinkDeviceAdapter(mLinkDeviceList);
        recyclerView.setAdapter(mLinkAdapter);
        //设备长按
        deviceEvent = new DeviceEvent(this);
        mLinkAdapter.setLongClick(new LinkDeviceAdapter.OnLongClickCallbackBlock() {
            @Override
            public void callBack(View v, int position) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                deviceEvent.deleteDevice(userPerson, position, new DeviceEvent.OnDeleteCallback() {
                                    @Override
                                    public void CallBack() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                                refreshLinkDevice();
                                            }
                                        });
                                    }
                                });
                                break;
                            case R.id.share:
                                if (userPerson.getDevices().get(position).getMode().equals("my")) {
                                    deviceEvent.shareDevice(userPerson, position);
                                } else {
                                    Toast.makeText(getApplicationContext(), "当前为共享设备，不能共享", Toast.LENGTH_SHORT).show();
                                }

                                break;
                        }
                        return true;
                    }
                });
            }
        });
//        motionLayout.transitionToEnd();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                motionLayout.transitionToStart();
//                motionLayout.setTransitionListener(MainActivity.this);
//            }
//        }, 50);
    }

    //初始化列表
    private void initLinkDevice() {
        mLinkDeviceList.clear();
        String[] strings = new String[20];
//        LinkDevice linkDevice1 = new LinkDevice("七彩灯", "share", R.drawable.esp8266, "d", "d");
        //通过邮箱查询当前用户的设备信息
        HttpUpdate.queryPersonFromServer(Common.url, Common.userEmail, new HttpUpdate.OnHttpReceivedCallbackBlock() {
            @Override
            public void callBack(Person person) {
                userPerson = person;
                if (userPerson.getStatus()) {       //成功
                    if (userPerson.getDevices() != null) {       //device不为空
                        List<Device> devices = userPerson.getDevices();
                        for (Device device : devices) {     //添加设备
                            String deviceName = device.getType().equals("rgb") ? "七彩灯" : "其他";
                            LinkDevice linkDevice = new LinkDevice(deviceName,
                                    device.getMode(), R.drawable.esp8266, device.getDeviceMac(), device.getAPMac());
                            mLinkDeviceList.add(linkDevice);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshLayout.finishRefresh();
                                mLinkAdapter.notifyDataSetChanged();
                                if (mLinkDeviceList.size() > 0) {
                                    deviceTitleTV.setText("我的设备(" + mLinkDeviceList.size() + ")");
                                    image2Layout.setVisibility(View.GONE);
                                } else {
                                    deviceTitleTV.setText("我的设备(0)");
                                    image2Layout.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                        for (int i = 0; i < mLinkDeviceList.size(); i++) {     //订阅主题
                            strings[i] = MqttUtil.SUB_TITLE + mLinkDeviceList.get(i).getAPBssid();
                            MqttUtil.SUB_TOPIC[i] = MqttUtil.SUB_TITLE + mLinkDeviceList.get(i).getAPBssid();
//                            MqttUtil.getInstance(MainActivity.this).subscribe(strings[i]);
                            Log.i("MainActivity", strings[i]);
                        }
//                         = strings;
                    }
                }
            }

            @Override
            public void failCallBack() {

            }
        });
    }

    private void refreshLinkDevice() {
        initLinkDevice();

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLinkDevice();
    }

    //设置主题
    private void setTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            setTheme(R.style.LightTheme);
        }
    }

    @Override
    public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
    }

    @Override
    public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {
        if (motionLayout == null) {
            return;
        }
        Log.i(TAG, String.valueOf(v));
        if (v - lastProgress > 0 && Math.abs(v - 1f) < 0.1f) {
            // from start to end
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                refreshLayout.setEnableRefresh(false);
            }
        } else if (v < 0.8f) {
            // from end to start
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(0);
                refreshLayout.setEnableRefresh(true);
            }
        }
        lastProgress = v;
    }

    @Override
    public void onTransitionCompleted(MotionLayout motionLayout, int i) {
    }

    @Override
    public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intentService);
        MqttUtil.release();
    }

    //侧滑菜单
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_share:        //分享设备
                Intent intent = new Intent(this, ShareActivity.class);
                Gson gson = new Gson();
                String transPerson = gson.toJson(userPerson, Person.class);
                intent.putExtra(TRANS_PERSON, transPerson);
                startActivity(intent);
                break;
            case R.id.nav_check:
//                HttpUtil.okHttpGetAPP("http://zyqfeiyue.gitee.io/test_pages/feiyue_version.json", new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) throws IOException {
//                        if (response.isSuccessful()) {
//                            assert response.body() != null;
//                            String responseData = response.body().string();
//                            Gson gson = new Gson();
//                            Version version = gson.fromJson(responseData, Version.class);
//                            Log.i("version", version.getVersionName());
//                            if (Integer.parseInt(version.getVersionCode()) > AppUtils.getVersionCode(MainActivity.this)) {
//                                mHandler.removeMessages(MSG_VERSION_UPDATE);
//                                mHandler.sendEmptyMessageDelayed(MSG_VERSION_UPDATE, 10);
//                            } else {
//                                mHandler.removeMessages(MSG_VERSION_LATEST);
//                                mHandler.sendEmptyMessageDelayed(MSG_VERSION_LATEST, 10);
//                            }
//                        }
//                    }
//                });
                break;
            case R.id.nav_revise:
                deviceEvent.reviseName();
                break;
            case R.id.nav_about:
                deviceEvent.showAPPInfo();
                break;
            case R.id.nav_off:
                deviceEvent.showLogOutDialog();
                break;
        }
        return true;
    }

    @OnClick({R.id.add_image, R.id.home_text, R.id.nav_open, R.id.add_image2, R.id.fab})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_image:
            case R.id.add_image2:
                Intent intent = new Intent(this, EspTouchActivity.class);
                startActivity(intent);
                break;
            case R.id.home_text:
            case R.id.nav_open:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.fab:
                Intent intentAsr = new Intent(this, AsrActivity.class);
                Gson gson = new Gson();
                String transPerson = gson.toJson(userPerson, Person.class);
                intentAsr.putExtra(TRANS_PERSON, transPerson);
                startActivity(intentAsr);
                break;
        }
    }
//    private Handler mHandler = new Handler(new Handler.Callback() {
//
//        @Override
//        public boolean handleMessage(@NonNull Message msg) {
//            if (msg.what == MSG_VERSION_UPDATE) {
//                UpdateConfig updateConfig = new UpdateConfig();
//                updateConfig.setCheckWifi(true);
//                updateConfig.setNeedCheckMd5(false);
//                updateConfig.setNotifyImgRes(R.mipmap.app_launcher);
//
//                UiConfig uiConfig = new UiConfig();
//                uiConfig.setUiType(UiType.PLENTIFUL);
//                UpdateAppUtils
//                        .getInstance()
//                        .apkUrl(apkUrl)
//                        .updateTitle(updateTitle)
//                        .updateContent(updateContent)
//                        .uiConfig(uiConfig)
//                        .updateConfig(updateConfig)
//                        .setMd5CheckResultListener(new Md5CheckResultListener() {
//                            @Override
//                            public void onResult(boolean result) {
//
//                            }
//                        })
//                        .setUpdateDownloadListener(new UpdateDownloadListener() {
//                            @Override
//                            public void onStart() {
//
//                            }
//
//                            @Override
//                            public void onDownload(int progress) {
//
//                            }
//
//                            @Override
//                            public void onFinish() {
//
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//
//                            }
//                        })
//                        .update();
//            } else if (msg.what == MSG_VERSION_LATEST) {
//                Toast.makeText(getApplicationContext(), "当前为最新版本", Toast.LENGTH_SHORT).show();
//            }
//            return true;
//        }
//    });
}
