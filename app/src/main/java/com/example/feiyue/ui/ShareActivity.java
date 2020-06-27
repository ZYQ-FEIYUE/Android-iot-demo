package com.example.feiyue.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.feiyue.MainActivity;
import com.example.feiyue.R;
import com.example.feiyue.adapter.ShareFragmentPagerAdapter;
import com.example.feiyue.bean.Person;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShareActivity extends AppCompatActivity {

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.back_btn_image)
    ImageButton backBtnImage;
    @BindView(R.id.back_btn)
    Button backBtn;
    @BindView(R.id.title_text)
    TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        ButterKnife.bind(this);
        //得到person对象
        Intent intent = getIntent();
        String transPerson = intent.getStringExtra(MainActivity.TRANS_PERSON);
        Gson gson = new Gson();
        Person person = gson.fromJson(transPerson, Person.class);
        ShareFragmentPagerAdapter adapter = new ShareFragmentPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, person);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        titleText.setText("共享管理");
    }

    @OnClick({R.id.back_btn_image, R.id.back_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn_image:
            case R.id.back_btn:
                finish();
                break;
        }
    }
}
