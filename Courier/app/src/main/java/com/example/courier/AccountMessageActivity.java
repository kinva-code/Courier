package com.example.courier;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class AccountMessageActivity extends AppCompatActivity {
    private ImageView back;
    private TextView edit;
    private TextView name;
    private TextView sex;
    private TextView age;
    private TextView birthday;
    private TextView phoneNumber;
    private TextView city;

    private HttpURLConnection urlConnection=null;
    private ApplicationUtil appUtil=new ApplicationUtil();
    private ProgressDialog progressDialog=null;
    private boolean timerClose;
    private MessageHandler messageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_message);
        getSupportActionBar().hide();     //隐藏标题栏
        setStatusBar();                   //状态栏透明
        back=(ImageView)findViewById(R.id.back);
        edit=(TextView)findViewById(R.id.edit);
        name=(TextView)findViewById(R.id.name);
        sex=(TextView)findViewById(R.id.sex);
        age=(TextView)findViewById(R.id.age);
        birthday=(TextView)findViewById(R.id.birthday);
        phoneNumber=(TextView)findViewById(R.id.phone_number);
        city=(TextView)findViewById(R.id.city);
        messageHandler=new MessageHandler();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AccountMessageActivity.this, EditAccountMessageActivity.class);
                intent.putExtra("id",appUtil.getUserId());
                intent.putExtra("name",name.getText().toString());
                intent.putExtra("sex",sex.getText().toString());
                intent.putExtra("birthday",birthday.getText().toString());
                intent.putExtra("age",age.getText().toString());
                intent.putExtra("phone_num",phoneNumber.getText().toString());
                intent.putExtra("city",city.getText().toString());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        progressDialog=ProgressDialog.show(AccountMessageActivity.this,"loading...","please wait...");
        getAccountMessage();
        new timer().start();
        Log.e("message",appUtil.getUserId());
    }

    public void getAccountMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String strUrl=appUtil.getServerUrl()+"/AccountMessageServlet";
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
                    jsonObject.put("id",appUtil.getUserId());
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
                        Message message=messageHandler.obtainMessage();
                        message.what=0;
                        message.obj=resultJSON;
                        messageHandler.sendMessage(message);
                    }else {
                        Message message=messageHandler.obtainMessage();
                        message.what=1;
                        message.obj=resultJSON;
                        messageHandler.sendMessage(message);
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
                    Message message=messageHandler.obtainMessage();
                    message.what=2;
                    messageHandler.sendMessage(message);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    try{
                        JSONObject contentJSON=(JSONObject)msg.obj;
                        name.setText(contentJSON.getString("name"));
                        sex.setText(contentJSON.getString("sex"));
                        age.setText(contentJSON.getString("age"));
                        birthday.setText(contentJSON.getString("birthday"));
                        phoneNumber.setText(contentJSON.getString("phone_num"));
                        city.setText(contentJSON.getString("city"));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    try{
                        JSONObject contentJSON=(JSONObject)msg.obj;
                        Toast.makeText(AccountMessageActivity.this,contentJSON.getString("reason"),Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AccountMessageActivity.this,"连接超时",Toast.LENGTH_SHORT).show();
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
