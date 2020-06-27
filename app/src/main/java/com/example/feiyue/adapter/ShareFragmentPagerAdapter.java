package com.example.feiyue.adapter;

import android.content.SharedPreferences;

import com.example.feiyue.bean.Device;
import com.example.feiyue.bean.Person;
import com.example.feiyue.fragment.ShareDeviceFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ShareFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragmentTab;
    private String[] tableTitle = new String[] {"共享", "接受"};
    private Person person;
    public ShareFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior, Person person) {
        super(fm, behavior);
        this.person = person;
        initFragmentTab();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentTab.get(position);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tableTitle[position];
    }

    private void initFragmentTab() {
        List<Device> devices;
        if (person.getDevices() != null) {
            devices = person.getDevices();
        } else {
            devices = new ArrayList<>();
        }
        List<Device> myDevices = new ArrayList<>();
        List<Device> receiveDevices = new ArrayList<>();
        for (Device device : devices) {
            if (device.getMode().equals("my")) {
                myDevices.add(device);
            } else {
                receiveDevices.add(device);
            }
        }
        ShareDeviceFragment shareDeviceFragment1 = new ShareDeviceFragment("share", myDevices);
        ShareDeviceFragment shareDeviceFragment2 = new ShareDeviceFragment("receive", receiveDevices);
        mFragmentTab = new ArrayList<>();
        mFragmentTab.add(shareDeviceFragment1);
        mFragmentTab.add(shareDeviceFragment2);
    }
}
