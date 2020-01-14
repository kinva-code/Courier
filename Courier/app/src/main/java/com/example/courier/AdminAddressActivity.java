package com.example.courier;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lljjcoder.citypickerview.widget.CityPicker;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class AdminAddressActivity extends AppCompatActivity {
    private ImageView back;
    private TextView finish;
    private TextView courierName;
    private TextView sex;
    private TextView birthday;
    private TextView age;
    private TextView phoneNumber;
    private TextView localCity;
    private TextView power;
    private RadioGroup addressRadio;
    private TextView new_power;

    private int rank=3;          //级别：1-省 2-市 3-区/县
    private String address="";      //当前所选具体地址

    private ApplicationUtil appUtil=new ApplicationUtil();
    private ProgressDialog progressDialog=null;
    private boolean timerClose;
    private HttpURLConnection urlConnection=null;
    private AdminAddressHandler adminAddressHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_address);
        getSupportActionBar().hide();     //隐藏标题栏
        setStatusBar();                   //状态栏透明
        back=(ImageView)findViewById(R.id.back);
        finish=(TextView)findViewById(R.id.finish);
        addressRadio=(RadioGroup)findViewById(R.id.address_radio);
        courierName=(TextView)findViewById(R.id.courier_name);
        sex=(TextView)findViewById(R.id.sex);
        birthday=(TextView)findViewById(R.id.birthday);
        age=(TextView)findViewById(R.id.age);
        phoneNumber=(TextView)findViewById(R.id.phone_number);
        localCity=(TextView)findViewById(R.id.local_city);
        power=(TextView)findViewById(R.id.power);
        new_power=(TextView)findViewById(R.id.new_power);
        adminAddressHandler=new AdminAddressHandler();

        courierName.setText(getIntent().getStringExtra("name"));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length=new_power.getText().toString().split("-").length;
                if(length!=rank){
                    Toast.makeText(AdminAddressActivity.this,"地址与所选权限不符，请重新选择",Toast.LENGTH_SHORT).show();
                }else {
                    update();
                }
            }
        });
        addressRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.province:
                        rank=1;
                        if(!address.equals("")){
                            String[] address_s=address.split("-");
                            new_power.setText(address_s[0]);
                        }
                        break;
                    case R.id.city:
                        rank=2;
                        if(!address.equals("")){
                            String[] address_s=address.split("-");
                            if(address_s.length>=2){
                                new_power.setText(address_s[0]+"-"+address_s[1]);
                            }
                        }
                        break;
                    case R.id.area:
                        rank=3;
                        if(!address.equals("")){
                            String[] address_s=address.split("-");
                            if(address_s.length==3){
                                new_power.setText(address_s[0]+"-"+address_s[1]+"-"+address_s[2]);
                            }
                        }
                        break;
                }
            }
        });
        new_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAddress();
            }
        });

        initUadateInfo();
    }

    public void selectAddress() {
        CityPicker cityPicker = new CityPicker.Builder(AdminAddressActivity.this)
                .textSize(14)
                .title("地址选择")
                .titleBackgroundColor("#FFFFFF")
                .confirTextColor("#696969")
                .cancelTextColor("#696969")
                .province("江苏省")
                .city("常州市")
                .district("天宁区")
                .textColor(Color.parseColor("#000000"))
                .provinceCyclic(true)
                .cityCyclic(false)
                .districtCyclic(false)
                .visibleItemsCount(7)
                .itemPadding(10)
                .onlyShowProvinceAndCity(false)
                .build();
        cityPicker.show();
        //监听方法，获取选择结果
        cityPicker.setOnCityItemClickListener(new CityPicker.OnCityItemClickListener() {
            @Override
            public void onSelected(String... citySelected) {
                //省份
                String provinceName = citySelected[0];
                //城市
                String cityName = citySelected[1];
                //区县（如果设定了两级联动，那么该项返回空）
                String districtName = citySelected[2];
                //邮编
                String code = citySelected[3];
                //为TextView赋值
                if(rank==1){
                    new_power.setText(provinceName);
                }else if(rank==2){
                    new_power.setText(provinceName+"-"+cityName);
                }else {
                    new_power.setText(provinceName+"-"+cityName+"-"+districtName);
                }
                address=new_power.getText().toString();
            }
        });
    }

    public void initUadateInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String strUrl=appUtil.getServerUrl()+"/InitUadateServlet";
                URL url=null;
                try{
                    url=new URL(strUrl);
                    urlConnection=(HttpURLConnection)url.openConnection();
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setUseCaches(false);
                    urlConnection.setRequestProperty("Content-Type","application/json");
                    urlConnection.setRequestProperty("Charset","utf-8");
                    urlConnection.connect();

                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("courier_id",getIntent().getStringExtra("courier_id"));
                    String content=String.valueOf(jsonObject);
                    OutputStream os=urlConnection.getOutputStream();
                    os.write(content.getBytes("utf-8"));
                    os.close();

                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String result="";
                    String readLine=null;
                    while ((readLine=bufferedReader.readLine())!=null){
                        result+=readLine;
                    }
                    bufferedReader.close();
                    urlConnection.disconnect();
                    JSONObject resultJSON=new JSONObject(URLDecoder.decode(result,"utf-8"));
                    if(resultJSON.getString("result").equals("true")){
                        Message message=adminAddressHandler.obtainMessage();
                        message.what=3;
                        message.obj=resultJSON;
                        adminAddressHandler.sendMessage(message);
                    }else {
                        Message message=adminAddressHandler.obtainMessage();
                        message.what=1;
                        message.obj=resultJSON;
                        adminAddressHandler.sendMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void update(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String strUrl=appUtil.getServerUrl()+"/UpdateAdminServlet";
                URL url=null;
                try{
                    url=new URL(strUrl);
                    urlConnection=(HttpURLConnection)url.openConnection();
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setUseCaches(false);
                    urlConnection.setRequestProperty("Content-Type","application/json");
                    urlConnection.setRequestProperty("Charset","utf-8");
                    urlConnection.connect();

                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("courier_id",getIntent().getStringExtra("courier_id"));
                    jsonObject.put("power",new_power.getText().toString());
                    String content=String.valueOf(jsonObject);
                    OutputStream os=urlConnection.getOutputStream();
                    os.write(content.getBytes("utf-8"));
                    os.close();

                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String result="";
                    String readLine=null;
                    while ((readLine=bufferedReader.readLine())!=null){
                        result+=readLine;
                    }
                    bufferedReader.close();
                    urlConnection.disconnect();
                    JSONObject resultJSON=new JSONObject(URLDecoder.decode(result,"utf-8"));
                    if(resultJSON.getString("result").equals("true")){
                        Message message=adminAddressHandler.obtainMessage();
                        message.what=0;
                        adminAddressHandler.sendMessage(message);
                        finish();
                    }else {
                        Message message=adminAddressHandler.obtainMessage();
                        message.what=1;
                        message.obj=resultJSON;
                        adminAddressHandler.sendMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class timer extends Thread{
        @Override
        public void run(){
            try{
                timerClose=false;
                Thread.sleep(3000);
                if(!timerClose){
                    timerClose=true;
                    urlConnection.disconnect();
                    Message message=adminAddressHandler.obtainMessage();
                    message.what=2;
                    adminAddressHandler.sendMessage(message);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class AdminAddressHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    break;
                case 1:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    try{
                        JSONObject reason=(JSONObject)msg.obj;
                        Toast.makeText(AdminAddressActivity.this,reason.getString("reason"),Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AdminAddressActivity.this,"连接超时",Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    try{
                        JSONObject result=(JSONObject)msg.obj;
                        courierName.setText(result.getString("name"));
                        sex.setText(result.getString("sex"));
                        birthday.setText(result.getString("birthday"));
                        age.setText(result.getString("age"));
                        phoneNumber.setText(result.getString("phone_num"));
                        localCity.setText(result.getString("city"));
                        power.setText(result.getString("power"));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
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
