package com.example.feiyue.fragment;

import android.app.Dialog;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.feiyue.R;
import com.example.feiyue.adapter.UserAdapter;
import com.example.feiyue.bean.Common;
import com.example.feiyue.bean.Device;
import com.example.feiyue.bean.Login;
import com.example.feiyue.bean.Person;
import com.example.feiyue.connect.HttpUpdate;
import com.example.feiyue.dialog.ASlideDialog;
import com.example.feiyue.ui.LoginActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

public class ShareDeviceFragment extends ListFragment {
    private static final String TAG = "ShareDeviceFragment";
    private String type;
    private List<Device> devices;
    public ShareDeviceFragment(String type, List<Device> devices) {
        this.type = type;
        this.devices = devices;
    }
    private SimpleAdapter simpleAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_device, container, false);
        simpleAdapter = new SimpleAdapter(view.getContext(), getData(), R.layout.list_item,
                new String[] {"user_image" , "title_text", "info_text"},
                new int[] {R.id.user_image, R.id.title_text, R.id.info_text});
        setListAdapter(simpleAdapter);
        return view;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        if (type.equals("share")) {
            final Dialog dialog = ASlideDialog.newInstance(getContext(), ASlideDialog.Gravity.Center, R.layout.share_user_dialog);
            ListView listView = dialog.findViewById(R.id.user_list_view);
            List<String> shares = devices.get(position).getShares();        //把分享的用户全部都包含进来
            List<String> listText = new ArrayList<>(shares);
            UserAdapter adapter = new UserAdapter(listText, dialog.getContext());
            listView.setAdapter(adapter);
            dialog.findViewById(R.id.user_ok_textview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<Integer, Boolean> map = adapter.getMap();
                    for (Integer key : map.keySet()) {
                        String shareUser = shares.get(key);
                        String deviceMac = devices.get(position).getDeviceMac();
                        //找到曾经分享过的用户获取person对象
                        HttpUpdate.queryPersonFromServer(Common.url, shareUser, new HttpUpdate.OnHttpReceivedCallbackBlock() {
                            @Override
                            public void callBack(Person person) {
                                if (person.getStatus()) {       //查询成功
                                    //创建设备
                                    List<Device> devices = new ArrayList<>(person.getDevices());
                                    //找到所属位置并删除
                                    for (Device device : devices) {
                                        if (device.getUser().equals(Common.userEmail) && device.getDeviceMac().equals(deviceMac)) {
                                            devices.remove(device);
                                            break;
                                        }
                                    }
                                    person.setDevices(devices);
                                    Gson gson = new Gson();     //上传服务器
                                    String jsonPerson = gson.toJson(person, Person.class);  //转为json字符串
                                    HttpUpdate.updatePersonToServer(Common.url, shareUser, jsonPerson, new HttpUpdate.OnHttpReceivedCallbackBlock() {
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
                        shares.remove(key.intValue());
                    }
                    //更新当前用户
                    devices.get(position).setShares(shares);
                    Person person = new Person();
                    person.setStatus(true);
                    person.setDevices(devices);
                    Gson gson = new Gson();
                    String personJson = gson.toJson(person, Person.class);
                    HttpUpdate.updatePersonToServer(Common.url, Common.userEmail, personJson, new HttpUpdate.OnHttpReceivedCallbackBlock() {
                        @Override
                        public void callBack(Person person) {
                            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        @Override
                        public void failCallBack() {
                        }
                    });
                    Log.i(TAG, String.valueOf(map));
                }
            });
            dialog.findViewById(R.id.user_cancel_textview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<>();
        for(Device device : devices) {
            Map<String, Object> map = new HashMap<>();
            map.put("user_image", R.drawable.esp8266);
            map.put("title_text", device.getType().equals("rgb") ? "七彩灯" : "其他");
            map.put("info_text", device.getUser());
            list.add(map);
        }
        return list;
    }
}
