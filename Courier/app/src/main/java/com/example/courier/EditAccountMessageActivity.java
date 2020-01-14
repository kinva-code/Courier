package com.example.courier;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
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
import java.util.Calendar;

public class EditAccountMessageActivity extends AppCompatActivity {
    private ImageView back;
    private TextView finish;
    private TextView newName;
    private TextView newSex;
    private TextView newBirthday;
    private TextView newAge;
    private EditText newPhoneNumber;
    private TextView newCity;

    private String userId;

    private PopupWindow popupWindow;
    private DatePicker datePicker;
    private int year;
    private int month;
    private int day;

    private ApplicationUtil appUtil=new ApplicationUtil();
    private ProgressDialog progressDialog=null;
    private boolean timerClose;
    private HttpURLConnection urlConnection=null;
    private EditMessageHandler editMessageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account_message);
        getSupportActionBar().hide();     //隐藏标题栏
        setStatusBar();                   //状态栏透明
        back=(ImageView)findViewById(R.id.back);
        finish=(TextView)findViewById(R.id.finish);
        newName=(TextView)findViewById(R.id.new_name);
        newSex=(TextView)findViewById(R.id.new_sex);
        newBirthday=(TextView)findViewById(R.id.new_birthday);
        newAge=(TextView)findViewById(R.id.new_age);
        newPhoneNumber=(EditText)findViewById(R.id.new_phone_number);
        newCity=(TextView) findViewById(R.id.new_city);
        editMessageHandler=new EditMessageHandler();

        setOnClickListener();
        init();
    }

    public void init(){
        Intent intent=getIntent();
        userId=intent.getStringExtra("id");
        newName.setText(intent.getStringExtra("name"));
        newSex.setText(intent.getStringExtra("sex"));
        newAge.setText(intent.getStringExtra("age"));
        newBirthday.setText(intent.getStringExtra("birthday"));
        newPhoneNumber.setText(intent.getStringExtra("phone_num"));
        newCity.setText(intent.getStringExtra("city"));
    }

    public void setOnClickListener(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog=ProgressDialog.show(EditAccountMessageActivity.this,"updateing...","please wait...");
                update();
                new timer().start();
            }
        });
        newSex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //软键盘收回
                InputMethodManager inputMethodManager=(InputMethodManager)EditAccountMessageActivity.this.getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(EditAccountMessageActivity.this.getWindow().getDecorView().getWindowToken(),0);

                View contentView= LayoutInflater.from(EditAccountMessageActivity.this).inflate(R.layout.select_sex,null);
                popupWindow=new PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,true);
                popupWindow.setContentView(contentView);
                TextView male=(TextView)contentView.findViewById(R.id.male);
                TextView female=(TextView)contentView.findViewById(R.id.female);
                male.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newSex.setText("男");
                        popupWindow.dismiss();
                    }
                });
                female.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newSex.setText("女");
                        popupWindow.dismiss();
                    }
                });
                View rootView=LayoutInflater.from(EditAccountMessageActivity.this).inflate(R.layout.activity_edit_account_message,null);
                popupWindow.showAtLocation(rootView, Gravity.BOTTOM,0,0);
            }
        });
        newBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //软键盘收回
                InputMethodManager inputMethodManager=(InputMethodManager)EditAccountMessageActivity.this.getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(EditAccountMessageActivity.this.getWindow().getDecorView().getWindowToken(),0);

                View contentView=LayoutInflater.from(EditAccountMessageActivity.this).inflate(R.layout.select_data,null);
                popupWindow=new PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,true);
                popupWindow.setContentView(contentView);
                datePicker=(DatePicker)contentView.findViewById(R.id.data_picker);
                initData();
                View rootView=LayoutInflater.from(EditAccountMessageActivity.this).inflate(R.layout.activity_edit_account_message,null);
                popupWindow.showAtLocation(rootView, Gravity.BOTTOM,0,0);
            }
        });
        newCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //软键盘收回
                InputMethodManager inputMethodManager=(InputMethodManager)EditAccountMessageActivity.this.getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(EditAccountMessageActivity.this.getWindow().getDecorView().getWindowToken(),0);
                selectAddress();//调用CityPicker选取区域
            }
        });
    }

    public void selectAddress() {
        CityPicker cityPicker = new CityPicker.Builder(EditAccountMessageActivity.this)
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
                newCity.setText(provinceName+"-"+cityName+"-"+districtName);
            }
        });
    }

    public void initData(){
        Calendar calendar=Calendar.getInstance();
        year=calendar.get(Calendar.YEAR);
        month=calendar.get(Calendar.MONTH);
        day=calendar.get(Calendar.DAY_OF_MONTH);
        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                newBirthday.setText(year+"-"+(monthOfYear+1)+"-"+dayOfMonth);
                setAge(year);
            }
        });
    }

    public void setAge(int birthday_year){
        long time=System.currentTimeMillis();
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int year = calendar.get(Calendar.YEAR);
        newAge.setText(String.valueOf(year-birthday_year));
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
                    Message message=editMessageHandler.obtainMessage();
                    message.what=2;
                    editMessageHandler.sendMessage(message);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void update(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String strUrl=appUtil.getServerUrl()+"/UpdateMessageServlet";
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
                    jsonObject.put("id",userId);
                    jsonObject.put("name",newName.getText().toString());
                    jsonObject.put("sex",newSex.getText().toString());
                    jsonObject.put("age",newAge.getText().toString());
                    jsonObject.put("birthday",newBirthday.getText().toString());
                    jsonObject.put("phone_num",newPhoneNumber.getText().toString());
                    jsonObject.put("city",newCity.getText().toString());
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
                        Message message=editMessageHandler.obtainMessage();
                        message.what=0;
                        editMessageHandler.sendMessage(message);
                        finish();
                    }else {
                        Message message=editMessageHandler.obtainMessage();
                        message.what=1;
                        message.obj=resultJSON;
                        editMessageHandler.sendMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private class EditMessageHandler extends Handler {
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
                        Toast.makeText(EditAccountMessageActivity.this,reason.getString("reason"),Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(EditAccountMessageActivity.this,"连接超时",Toast.LENGTH_SHORT).show();
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
