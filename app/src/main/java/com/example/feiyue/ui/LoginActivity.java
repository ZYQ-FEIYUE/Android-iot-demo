package com.example.feiyue.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.feiyue.MainActivity;
import com.example.feiyue.R;
import com.example.feiyue.bean.Common;
import com.example.feiyue.bean.Login;
import com.example.feiyue.bean.Person;
import com.example.feiyue.connect.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.motionLayout)
    MotionLayout loginRootView;
    @BindView(R.id.login_btn)
    Button loginBtn;
    @BindView(R.id.register_btn)
    Button registerBtn;
    @BindView(R.id.email_edit)
    EditText emailET;
    @BindView(R.id.password_edit)
    EditText passwordET;
    private boolean isShowBoard = true;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        loginRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isShowBoard) {
                    int heightDiff = loginRootView.getRootView().getHeight() - loginRootView.getHeight();
                    if (heightDiff > dpToPx(LoginActivity.this, 200)) { // if more than 200 dp, it's probably a keyboard...
                        // ... do something here
                        Log.d("----------", "打开");
                        loginRootView.transitionToEnd();

                    } else {
                        Log.d("----------", "关闭");
                        loginRootView.transitionToStart();
                    }
                }
            }
        });
    }

    //dp转px
    public float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    @OnClick({R.id.login_btn, R.id.register_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_btn:
                String email = emailET.getText().toString().trim();
                String password = passwordET.getText().toString().trim();
                if (!"".equals(email) && !"".equals(password)) {
                    showProgressDialog();
                    String path = "http://8.129.61.208:8080/AndroidTest/Login?email="+email+"&password="+password;
                    HttpUtil.okHttpGet(path, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    Toast.makeText(getApplicationContext(), "请检查网络", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.isSuccessful()) {
                                assert response.body() != null;
                                String result = response.body().string();
                                //处理UI需要切换到UI线程处理
                                //                String responseData = response.body().string();
                                Gson gson = new Gson();
                                Person person = gson.fromJson(result, Person.class);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (person.getStatus()) {
                                            closeProgressDialog();
                                            Common.userEmail = email;
                                            Login login = new Login(LoginActivity.this);
                                            login.setEmail(email);
                                            login.setIsLogin(true);
                                            login.setName(person.getName());
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                            Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                                        } else {
                                            closeProgressDialog();
                                            Toast.makeText(getApplicationContext(), "登录失败，邮箱或密码错误", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                Log.i("MAIN", "onResponse: " + result);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "信息输入不完整", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.register_btn:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在登录...");
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
