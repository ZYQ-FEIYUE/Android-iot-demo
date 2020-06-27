package com.example.feiyue.connect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;


public class MyMqttService extends Service {

    public static void startService(Context context) {
        context.startService(new Intent(context, MyMqttService.class));
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = this;
        MqttUtil mqttUtil = MqttUtil.getInstance(context);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
