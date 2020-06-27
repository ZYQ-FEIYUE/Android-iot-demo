package com.example.feiyue.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.example.feiyue.MainActivity;
import com.example.feiyue.R;
import com.example.feiyue.bean.Common;
import com.example.feiyue.bean.Login;

public class StartActivity extends AppCompatActivity implements MotionLayout.TransitionListener {
    private final int MSG_FINISH = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        MotionLayout motionLayout = findViewById(R.id.motionLayout1);
        motionLayout.transitionToEnd();
        motionLayout.setTransitionListener(this);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                /*
//                 *要执行的操作
//                 */
//                Intent intent = new Intent(StartActivity.this, MainActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        }, 1000);//1秒后执行

    }
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_FINISH) {
                Login login = new Login(StartActivity.this);
                if (login.getIsLogin()) {       //已经登录，打开main
                    Common.userEmail = login.getEmail();
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {        //没有登录，打开登录界面
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                finish();
                mHandler.removeCallbacksAndMessages(null);
            }
            return true;
        }
    });
    @Override
    public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
    }

    @Override
    public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {
    }

    @Override
    public void onTransitionCompleted(MotionLayout motionLayout, int i) {
        Log.i("START", "ddsdf");
        mHandler.removeMessages(MSG_FINISH);
        mHandler.sendEmptyMessageDelayed(MSG_FINISH, 50);
    }

    @Override
    public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

    }
}
