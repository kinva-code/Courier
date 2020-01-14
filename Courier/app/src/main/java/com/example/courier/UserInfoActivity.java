package com.example.courier;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class UserInfoActivity extends AppCompatActivity {
    private ImageView back;
    private TextView name;
    private TextView sex;
    private TextView age;
    private TextView phoneNumber;
    private TextView city;
    private TextView address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        getSupportActionBar().hide();     //隐藏标题栏
        setStatusBar();                   //状态栏透明
        back=(ImageView)findViewById(R.id.back);
        name=(TextView)findViewById(R.id.name);
        sex=(TextView)findViewById(R.id.sex);
        age=(TextView)findViewById(R.id.age);
        phoneNumber=(TextView)findViewById(R.id.phone_number);
        city=(TextView)findViewById(R.id.city);
        address=(TextView)findViewById(R.id.address);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        name.setText(getIntent().getStringExtra("name"));
        sex.setText(getIntent().getStringExtra("sex"));
        age.setText(getIntent().getStringExtra("age"));
        phoneNumber.setText(getIntent().getStringExtra("phone_num"));
        city.setText(getIntent().getStringExtra("city"));
        address.setText(getIntent().getStringExtra("address"));
    }

    public void setStatusBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorAccent));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4 全透明状态栏
            Window window = getWindow();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.colorAccent));
        }
    }
}
