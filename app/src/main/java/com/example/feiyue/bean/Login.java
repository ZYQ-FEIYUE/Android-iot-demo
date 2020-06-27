package com.example.feiyue.bean;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class Login {
    private Context mContext;
    private final static String LOGIN_KEY = "login_key1";
    public final static String LOGIN_EMAIL = "login_email";
    private final static String LOGIN_NAME = "login_name";
    public Login(Context context) {
        mContext = context;
    }
    public void setIsLogin(boolean isLogin) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOGIN_KEY, isLogin);
        editor.apply();
    }
    public boolean getIsLogin() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getBoolean(LOGIN_KEY, false);
    }
    public void setEmail(String email) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LOGIN_EMAIL, email);
        editor.apply();
    }
    public String getEmail() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getString(LOGIN_EMAIL, "");
    }
    public void setName(String name) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LOGIN_NAME, name);
        editor.apply();
    }
    public String getName() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getString(LOGIN_NAME, "");
    }
}
