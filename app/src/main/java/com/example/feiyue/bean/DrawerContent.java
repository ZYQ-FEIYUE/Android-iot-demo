package com.example.feiyue.bean;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.drawerlayout.widget.DrawerLayout;

public class DrawerContent extends MotionLayout implements DrawerLayout.DrawerListener {
    public DrawerContent(Context context) {
        super(context);
    }

    public DrawerContent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerContent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        this.setProgress(slideOffset);
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent viewParent = this.getParent();
        if (!(viewParent instanceof DrawerLayout)) {
            viewParent = null;
        }
        DrawerLayout var1 = (DrawerLayout)viewParent;
        if (var1 != null) {
            var1.addDrawerListener((DrawerLayout.DrawerListener)this);
        }
    }
}
