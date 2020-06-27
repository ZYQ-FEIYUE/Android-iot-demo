package com.example.feiyue.bean;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewParent;

import androidx.constraintlayout.motion.widget.MotionLayout;
import com.google.android.material.appbar.AppBarLayout;

public class CollapsibleToolbar extends MotionLayout implements AppBarLayout.OnOffsetChangedListener {
    public CollapsibleToolbar(Context context) {
        super(context);
    }

    public CollapsibleToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CollapsibleToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        this.setProgress((float)-verticalOffset / appBarLayout.getTotalScrollRange());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent viewParent = this.getParent();
        if (!(viewParent instanceof AppBarLayout)) {
            viewParent = null;
        }
        AppBarLayout var1 = (AppBarLayout)viewParent;
        if (var1 != null) {
            var1.addOnOffsetChangedListener((AppBarLayout.OnOffsetChangedListener)this);
        }
    }
}
