package com.example.feiyue.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.feiyue.R;
import com.example.feiyue.bean.Person;
import com.example.feiyue.connect.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.back_btn_image)
    ImageButton backBtnIV;
    @BindView(R.id.back_btn)
    Button backBtn;
    @BindView(R.id.name_edit)
    EditText nameET;
    @BindView(R.id.email_edit)
    EditText emailET;
    @BindView(R.id.password_edit)
    EditText passwordET;
    @BindView(R.id.register_btn)
    Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.back_btn_image, R.id.back_btn, R.id.register_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn_image:       //关闭当前活动
            case R.id.back_btn:
                finish();
                break;
            case R.id.register_btn:
                String name = nameET.getText().toString().trim();
                String email = emailET.getText().toString().trim();
                String password = passwordET.getText().toString().trim();
                Log.e("TAG", name);
                if (!"".equals(email) && !"".equals(password) && !"".equals(name)) {
                    String path = "http://8.129.61.208:8080/AndroidTest/Register?name="+name+"&email="+email+"&password="+password;
//                    String usernameString = new String(path.getBytes("ISO-8859-1"),"UTF-8");
                    HttpUtil.okHttpGet(path, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "请检查网络", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.isSuccessful()) {
                                assert response.body() != null;
                                String result = response.body().string();
                                Gson gson = new Gson();
                                Person person = gson.fromJson(result, Person.class);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (person.getStatus()) {
                                            Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "当前邮箱已注册", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                //处理UI需要切换到UI线程处理
                                //                String responseData = response.body().string();
                                Log.i("MAIN", "onResponse: " + result);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "信息输入不完整", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}
