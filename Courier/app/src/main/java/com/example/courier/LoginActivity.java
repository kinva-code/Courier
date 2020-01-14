package com.example.courier;

import androidx.appcompat.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class LoginActivity extends AppCompatActivity {
    private ImageView back;
    private EditText account;
    private EditText password;
    private Button login;
    private TextView forget_password;
    private TextView registe;

    private String textAccount;
    private String textPassword;

    private ApplicationUtil appUtil=new ApplicationUtil();
    private ProgressDialog progressDialog=null;
    private boolean timerClose;
    private LoginHandler loginHandler;

    private File file;
    private FileOutputStream fileOutputStream;
    private FileInputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader fileBufferedReader;
    private HttpURLConnection urlConnection=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();     //隐藏标题栏
        setStatusBar();                   //状态栏透明
        inflate();
        setOnClickListener();
    }

    public void inflate(){
        back=(ImageView)findViewById(R.id.back);
        account=(EditText)findViewById(R.id.account);
        password=(EditText)findViewById(R.id.password);
        login=(Button)findViewById(R.id.login);
        forget_password=(TextView)findViewById(R.id.forget_password);
        registe=(TextView)findViewById(R.id.registe);

        loginHandler=new LoginHandler();
    }

    public void setOnClickListener(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("FBI WARNING")
                        .setMessage("自己忘记密码活该，重新注册去吧"+"\n"+"(其实是我懒得写了)")
                        .setPositiveButton("确定",null)
                        .show();
            }
        });
        registe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisteActivity.class);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textAccount=account.getText().toString();
                textPassword=password.getText().toString();
                if(!textAccount.equals("")&&!textPassword.equals("")){
                    try{
                        progressDialog= ProgressDialog.show(LoginActivity.this,"logining...","please wait...");
                        startLogin();
                        new timer().start();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else if((!textAccount.equals(""))&&(textPassword.equals(""))){
                    Toast.makeText(LoginActivity.this,"请输入密码",Toast.LENGTH_SHORT).show();
                }else if((textAccount.equals(""))&&(!textPassword.equals(""))){
                    Toast.makeText(LoginActivity.this,"请输入账号",Toast.LENGTH_SHORT).show();
                }else if((textAccount.equals(""))&&(textPassword.equals(""))){
                    Toast.makeText(LoginActivity.this,"请输入账号和密码",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void startLogin(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String strUrl=appUtil.getServerUrl()+"/LoginServlet";
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
                    jsonObject.put("name",account.getText().toString());
                    jsonObject.put("password",password.getText().toString());
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
                        Message message=loginHandler.obtainMessage();
                        message.what=0;
                        loginHandler.sendMessage(message);
                        loginSucceed(resultJSON.getString("id"));
                        if(resultJSON.getString("isAdmin").equals("true")){
                            appUtil.setIsAdmin(true);
                        }else {
                            appUtil.setIsAdmin(false);
                        }
                        finish();
                    }else {
                        Message message=loginHandler.obtainMessage();
                        message.what=1;
                        message.obj=resultJSON;
                        loginHandler.sendMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void loginSucceed(String id){
        Log.e("error","loginSucceed()");
        file=new File(appUtil.getDirPath()+"/Users/UserAccount.txt");
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch (Exception e){e.printStackTrace();}
        }
        try{
            inputStream=new FileInputStream(appUtil.getDirPath()+"/Users/UserAccount.txt");
            inputStreamReader=new InputStreamReader(inputStream);
            fileBufferedReader=new BufferedReader(inputStreamReader);
            String text="";
            String fileText="";
            int i=0;
            while ((text=fileBufferedReader.readLine())!=null){
                if(i==0){
                    fileText=fileText + text;
                    i++;
                }else {
                    fileText=fileText + "&" + text;
                }
                String[] tests=text.split("#");
            }
            fileBufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            try{
                fileOutputStream=new FileOutputStream(file,false);
                fileOutputStream.write(("").getBytes("utf-8"));
            }catch (Exception e){e.printStackTrace();}finally {
                fileOutputStream.close();
            }
            try{
                fileOutputStream=new FileOutputStream(file,true);
                String[] fileTexts=fileText.split("&");
                int j=0;
                boolean isNewAccount=true;
                Log.e("error","fileText="+fileText);
                Log.e("error","fileTexts.length="+fileTexts.length);
                if (fileText.equals("")){
                    fileOutputStream.write((id+"#"+textAccount+"#"+textPassword+"#true"+"\n").getBytes("utf-8"));
                }else{
                    while (j<fileTexts.length){
                        String[] fileTexts_s=fileTexts[j].split("#");
                        if (fileTexts_s[0].equals(id)) {
                            isNewAccount=false;
                            fileOutputStream.write((id+"#"+textAccount+"#"+textPassword+"#true"+"\n").getBytes("utf-8"));
                        }else {
                            fileOutputStream.write((fileTexts_s[0]+"#"+fileTexts_s[1]+"#"+fileTexts_s[2]+"#false"+"\n").getBytes("utf-8"));
                        }
                        j++;
                    }
                    if(isNewAccount){
                        Log.e("error","textAccount+\"#\"+textPassword+\"#true\\n\"");
                        fileOutputStream.write((id+"#"+textAccount+"#"+textPassword+"#true"+"\n").getBytes("utf-8"));
                    }
                }
                appUtil.setUserId(id);
                appUtil.setUserName(textAccount);
                appUtil.setUserPassword(textPassword);
                appUtil.setIsLogin(true);
            }catch (Exception e){ }finally {
                fileOutputStream.close();
            }
        }catch (Exception e){e.printStackTrace();}
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
                    Message message=loginHandler.obtainMessage();
                    message.what=2;
                    loginHandler.sendMessage(message);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class LoginHandler extends Handler {
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
                        Toast.makeText(LoginActivity.this,reason.getString("reason"),Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(LoginActivity.this,"连接超时",Toast.LENGTH_SHORT).show();
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
