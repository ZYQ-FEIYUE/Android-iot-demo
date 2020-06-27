package com.example.feiyue.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.TouchNetUtil;
import com.example.feiyue.FeiYueApp;
import com.example.feiyue.R;
import com.example.feiyue.bean.Common;
import com.example.feiyue.bean.Device;
import com.example.feiyue.bean.Person;
import com.example.feiyue.connect.HttpUpdate;
import com.example.feiyue.connect.MqttUtil;
import com.example.feiyue.esp.EspTouchViewModel;
import com.example.feiyue.esp.EspTouchActivityAbs;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EspTouchActivity extends EspTouchActivityAbs {

    @BindView(R.id.back_btn_image)
    ImageButton backBtnIV;
    @BindView(R.id.back_btn)
    Button backBtn;
    @BindView(R.id.apSsidText)
    TextView apSsidTV;
//    @BindView(R.id.apBssidText)
//    TextView apBssidTV;
    @BindView(R.id.apPasswordEdit)
    EditText apPasswordEdit;
//    @BindView(R.id.deviceCountEdit)
//    TextInputEditText deviceCountEdit;
//    @BindView(R.id.packageModeGroup)
//    RadioGroup packageModeGroup;
    @BindView(R.id.messageView)
    TextView messageView;
    @BindView(R.id.confirmBtn)
    Button confirmBtn;
    private static final String TAG = EspTouchActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSION = 0x01;

    private EspTouchViewModel mViewModel;

    private EsptouchAsyncTask4 mTask;
    private static String APBssid;      //存储路由器mac
    private static String deviceBssid;  //存储设备mac
    private static final int MSG_UPDATE = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esp_touch);
        ButterKnife.bind(this);
        mViewModel = new EspTouchViewModel();       //实例化
        mViewModel.apSsidTV = apSsidTV;
//        mViewModel.apBssidTV = apBssidTV;
        mViewModel.apPasswordEdit = apPasswordEdit;
//        mViewModel.deviceCountEdit = deviceCountEdit;
//        mViewModel.packageModeGroup = packageModeGroup;
        mViewModel.messageView = messageView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {       //申请权限
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, REQUEST_PERMISSION);
        }
        FeiYueApp.getInstance().observeBroadcast(EspTouchActivity.this, broadcast -> {
            Log.d(TAG, "onCreate: Broadcast=" + broadcast);
            onWifiChanged();
        });
    }
    private static Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_UPDATE) {
                HttpUpdate.queryPersonFromServer(Common.url, Common.userEmail, new HttpUpdate.OnHttpReceivedCallbackBlock() {
                            @Override
                            public void callBack(Person person) {
                                if (person.getStatus()) {       //查询成功
                                    //创建设备
                                    Device device = new Device(1, "rgb", "my", Common.userEmail, deviceBssid, APBssid, new ArrayList<>());
                                    List<Device> devices;
                                    if (person.getDevices()!=null) {       //device不为空
                                        devices = person.getDevices();
                                    } else {    //用户设备为空
                                        devices = new ArrayList<>();
                                    }
                                    devices.add(device);
                                    person.setDevices(devices);
                                    Gson gson = new Gson();
                                    String jsonPerson = gson.toJson(person, Person.class);  //转为json字符串
                                    HttpUpdate.updatePersonToServer(Common.url, Common.userEmail, jsonPerson, new HttpUpdate.OnHttpReceivedCallbackBlock() {
                                        @Override
                                        public void callBack(Person person) {

                                        }
                                        @Override
                                        public void failCallBack() {
                                        }
                                    });
                                }
                            }

                            @Override
                            public void failCallBack() {

                            }
                        });

            }
            return true;
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.back_btn_image, R.id.back_btn, R.id.confirmBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn_image:
            case R.id.back_btn:
                finish();
                break;
            case R.id.confirmBtn:
                executeEsptouch();
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onWifiChanged();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.esptouch1_location_permission_title)
                        .setMessage(R.string.esptouch1_location_permission_message)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                        .show();
            }

            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected String getEspTouchVersion() {
        return getString(R.string.esptouch1_about_version, IEsptouchTask.ESPTOUCH_VERSION);
    }

    private StateResult check() {
        StateResult result = checkPermission();     //检查权限
        if (!result.permissionGranted) {
            return result;
        }
        result = checkLocation();       //检查gps
        result.permissionGranted = true;
        if (result.locationRequirement) {
            return result;
        }
        result = checkWifi();
        result.permissionGranted = true;
        result.locationRequirement = false;
        return result;
    }

    private void onWifiChanged() {
        StateResult stateResult = check();
        mViewModel.message = stateResult.message;
        mViewModel.ssid = stateResult.ssid;
        mViewModel.ssidBytes = stateResult.ssidBytes;
        mViewModel.bssid = stateResult.bssid;
        mViewModel.confirmEnable = false;
        if (stateResult.wifiConnected) {
            mViewModel.confirmEnable = true;
            if (stateResult.is5G) {
                mViewModel.message = getString(R.string.esptouch1_wifi_5g_message);
            }
        } else {
            if (mTask != null) {
                mTask.cancelEsptouch();
                mTask = null;
                new AlertDialog.Builder(EspTouchActivity.this)
                        .setMessage(R.string.esptouch1_configure_wifi_change_message)
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        }
        mViewModel.invalidateAll();
    }
    //执行esptouch
    private void executeEsptouch() {
        EspTouchViewModel viewModel = mViewModel;
        byte[] ssid = viewModel.ssidBytes == null ? ByteUtil.getBytesByString(viewModel.ssid)
                : viewModel.ssidBytes;
        CharSequence pwdStr = mViewModel.apPasswordEdit.getText();
        byte[] password = pwdStr == null ? null : ByteUtil.getBytesByString(pwdStr.toString());
        byte[] bssid = TouchNetUtil.parseBssid2bytes(viewModel.bssid);
        //处理需上传至服务器的mac
        String[] text = viewModel.bssid.split(":");
        StringBuilder sb = new StringBuilder();
        for (String s : text) {
            sb.append(s);
        }
        APBssid = sb.toString();
        Log.i(TAG, APBssid);

//        CharSequence devCountStr = mViewModel.deviceCountEdit.getText();
        CharSequence devCountStr = "1";
        byte[] deviceCount = devCountStr == null ? new byte[0] : devCountStr.toString().getBytes();
//        byte[] broadcast = {(byte) (mViewModel.packageModeGroup.getCheckedRadioButtonId() == R.id.packageBroadcast
//                ? 1 : 0)};
        byte[] broadcast = {(byte) 0};

        if (mTask != null) {
            mTask.cancelEsptouch();
        }
        mTask = new EsptouchAsyncTask4(this);
        mTask.execute(ssid, bssid, password, deviceCount, broadcast);
    }

    private static class EsptouchAsyncTask4 extends AsyncTask<byte[], IEsptouchResult, List<IEsptouchResult>> {
        private WeakReference<EspTouchActivity> mActivity;

        private final Object mLock = new Object();
        private ProgressDialog mProgressDialog;
        private AlertDialog mResultDialog;
        private IEsptouchTask mEsptouchTask;

        EsptouchAsyncTask4(EspTouchActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        void cancelEsptouch() {
            cancel(true);
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if (mResultDialog != null) {
                mResultDialog.dismiss();
            }
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
        }

        @Override
        protected void onPreExecute() {
            Activity activity = mActivity.get();
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage(activity.getString(R.string.esptouch1_configuring_message));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(dialog -> {
                synchronized (mLock) {
                    if (mEsptouchTask != null) {
                        mEsptouchTask.interrupt();
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getText(android.R.string.cancel),
                    (dialog, which) -> {
                        synchronized (mLock) {
                            if (mEsptouchTask != null) {
                                mEsptouchTask.interrupt();
                            }
                        }
                    });
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(IEsptouchResult... values) {
            Context context = mActivity.get();
            if (context != null) {
                IEsptouchResult result = values[0];
                Log.i(TAG, "EspTouchResult: " + result);
                String text = result.getBssid() + " is connected to the wifi";      //配网完成
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected List<IEsptouchResult> doInBackground(byte[]... params) {
            EspTouchActivity activity = mActivity.get();
            int taskResultCount;
            synchronized (mLock) {
                byte[] apSsid = params[0];
                byte[] apBssid = params[1];
                byte[] apPassword = params[2];
                byte[] deviceCountData = params[3];
                byte[] broadcastData = params[4];
                taskResultCount = deviceCountData.length == 0 ? -1 : Integer.parseInt(new String(deviceCountData));
                Context context = activity.getApplicationContext();
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, context);
                mEsptouchTask.setPackageBroadcast(broadcastData[0] == 1);
                mEsptouchTask.setEsptouchListener(this::publishProgress);
            }
            return mEsptouchTask.executeForResults(taskResultCount);
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            EspTouchActivity activity = mActivity.get();
            activity.mTask = null;
            mProgressDialog.dismiss();
            if (result == null) {
                mResultDialog = new AlertDialog.Builder(activity)
                        .setMessage(R.string.esptouch1_configure_result_failed_port)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                mResultDialog.setCanceledOnTouchOutside(false);
                return;
            }

            // check whether the task is cancelled and no results received
            IEsptouchResult firstResult = result.get(0);
            if (firstResult.isCancelled()) {
                return;
            }
            // the task received some results including cancelled while
            // executing before receiving enough results

            if (!firstResult.isSuc()) {
                mResultDialog = new AlertDialog.Builder(activity)
                        .setMessage(R.string.esptouch1_configure_result_failed)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                mResultDialog.setCanceledOnTouchOutside(false);
                return;
            }

            ArrayList<CharSequence> resultMsgList = new ArrayList<>(result.size());
            for (IEsptouchResult touchResult : result) {
                String message = activity.getString(R.string.esptouch1_configure_result_success_item,
                        touchResult.getBssid(), touchResult.getInetAddress().getHostAddress());
                resultMsgList.add(message);
            }
            //上传设备mac路由器mac至服务器
            deviceBssid = result.get(0).getBssid();
            Log.i(TAG, deviceBssid);
            MqttUtil.getInstance(activity).subscribe(MqttUtil.SUB_TITLE + APBssid);
            mHandler.removeMessages(MSG_UPDATE);        //更新数据
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE, 5);

            CharSequence[] items = new CharSequence[resultMsgList.size()];
            mResultDialog = new AlertDialog.Builder(activity)
                    .setTitle(R.string.esptouch1_configure_result_success)
                    .setItems(resultMsgList.toArray(items), null)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            mResultDialog.setCanceledOnTouchOutside(false);
        }
    }
}
