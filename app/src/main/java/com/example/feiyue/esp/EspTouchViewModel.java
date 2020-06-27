package com.example.feiyue.esp;

import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class EspTouchViewModel {
    public TextView apSsidTV;
//    public TextView apBssidTV;
    public EditText apPasswordEdit;
//    public EditText deviceCountEdit;
//    public RadioGroup packageModeGroup;
    public TextView messageView;
    public Button confirmBtn;

    public String ssid;
    public byte[] ssidBytes;
    public String bssid;

    public CharSequence message;

    public boolean confirmEnable;

    public void invalidateAll() {
        apSsidTV.setText(ssid);
//        apBssidTV.setText(bssid);
        messageView.setText(message);
//        confirmBtn.setEnabled(confirmEnable);
    }
}
