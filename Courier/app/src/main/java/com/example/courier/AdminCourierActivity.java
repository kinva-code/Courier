package com.example.courier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class AdminCourierActivity extends AppCompatActivity {
    private ImageView back;
    private List<Courier> courierList=new ArrayList<>();
    private RecyclerView courierRecyclerView;
    private CourierAdapter courierAdapter;

    private ApplicationUtil appUtil=new ApplicationUtil();
    private ProgressDialog progressDialog=null;
    private boolean timerClose;
    private AdminCourierHandler adminCourierHandler;
    private HttpURLConnection urlConnection=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_courier);
        getSupportActionBar().hide();     //隐藏标题栏
        setStatusBar();                   //状态栏透明
        back=(ImageView)findViewById(R.id.back);
        courierRecyclerView=(RecyclerView)findViewById(R.id.courier_recycle_view);
        adminCourierHandler=new AdminCourierHandler();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        courierRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this,R.drawable.shape_small_divider));
        courierRecyclerView.addItemDecoration(dividerItemDecoration);
        courierAdapter=new CourierAdapter(courierList,AdminCourierActivity.this);
        courierRecyclerView.setAdapter(courierAdapter);

        initCourier();
    }

    public void initCourier(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String strUrl=appUtil.getServerUrl()+"/InitCourierServlet";
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
                    Log.e("error", URLDecoder.decode(result,"utf-8"));
                    JSONObject resultJSON=new JSONObject(URLDecoder.decode(result,"utf-8"));
                    if(resultJSON.getString("result").equals("true")){
                        Message message=adminCourierHandler.obtainMessage();
                        message.what=0;
                        message.obj=resultJSON;
                        adminCourierHandler.sendMessage(message);
                    }else {
                        Message message=adminCourierHandler.obtainMessage();
                        message.what=1;
                        message.obj=resultJSON;
                        adminCourierHandler.sendMessage(message);
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
                    Message message=adminCourierHandler.obtainMessage();
                    message.what=2;
                    adminCourierHandler.sendMessage(message);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class AdminCourierHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    try{
                        JSONObject resultJSON=(JSONObject)msg.obj;
                        int length=resultJSON.getInt("length");
                        Log.e("length",String.valueOf(length));
                        courierList.clear();
                        for(int i=0;i<length;i++){
                            String content=resultJSON.getString("courier"+i);
                            String[] contents=content.split("#");
                            Courier courier=new Courier(contents[0],contents[1]);
                            courierList.add(courier);
                        }
                        courierAdapter.notifyDataSetChanged();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    try{
                        JSONObject reason=(JSONObject)msg.obj;
                        Toast.makeText(AdminCourierActivity.this,reason.getString("reason"),Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AdminCourierActivity.this,"连接超时",Toast.LENGTH_SHORT).show();
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
