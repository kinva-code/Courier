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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.List;

public class AccountManageActivity extends AppCompatActivity {
    private ImageView back;
    private TextView addAccount;
    private TextView logout;
    private String userId;
    private String name;
    private String password;

    private ListView accountListView;
    private List<Account> accountList=new ArrayList<Account>();
    private AccountAdapter accountAdapter;

    private File file;
    private FileOutputStream fileOutputStream;
    private FileInputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader fileBufferedReader;

    private ApplicationUtil appUtil=new ApplicationUtil();
    private ProgressDialog progressDialog=null;
    private boolean timerClose;
    private HttpURLConnection urlConnection=null;
    private AccountManageHandler accountManageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manage);
        getSupportActionBar().hide();     //隐藏标题栏
        setStatusBar();                   //状态栏透明
        inflate();
        setOnClickListener();
    }

    public void inflate(){
        back=(ImageView)findViewById(R.id.back);
        addAccount=(TextView)findViewById(R.id.add_account);
        logout=(TextView)findViewById(R.id.logout);
        accountListView=(ListView)findViewById(R.id.account_list_view);
        accountManageHandler=new AccountManageHandler();
    }

    public void setOnClickListener(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AccountManageActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        accountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userId=accountList.get(position).getId();
                name=accountList.get(position).getName();
                password=accountList.get(position).getPassword();
                progressDialog=ProgressDialog.show(AccountManageActivity.this,"logining...","please wait...");
                startLogin();
                new timer().start();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    inputStream=new FileInputStream(appUtil.getDirPath()+"/Users/UserAccount.txt");
                    inputStreamReader=new InputStreamReader(inputStream);
                    fileBufferedReader=new BufferedReader(inputStreamReader);
                    String text="";
                    String fileText="";
                    int i=0;
                    accountList.clear();
                    while ((text=fileBufferedReader.readLine())!=null){
                        if(i==0){
                            fileText=fileText + text;
                            i++;
                        }else {
                            fileText=fileText + "&" + text;
                        }
                        String[] tests=text.split("#");
                        Account account=new Account(tests[0],tests[1],tests[2],"false");
                        accountList.add(account);
                    }
                    fileBufferedReader.close();
                    inputStreamReader.close();
                    inputStream.close();
                    accountAdapter.notifyDataSetChanged();
                    appUtil.setUserId("");
                    appUtil.setUserName("");
                    appUtil.setUserPassword("");
                    appUtil.setIsLogin(false);
                    appUtil.setIsAdmin(false);

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
                        while (j<fileTexts.length){
                            String[] fileTexts_s=fileTexts[j].split("#");
                            fileOutputStream.write((fileTexts_s[0]+"#"+fileTexts_s[1]+"#"+fileTexts_s[2]+"#false"+"\n").getBytes("utf-8"));
                            j++;
                        }
                    }catch (Exception e){ }finally {
                        fileOutputStream.close();
                    }
                }catch (Exception e){

                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        createFile();
        init();
    }

    public void createFile(){
        File dir=new File(appUtil.getDirPath()+"/Users");
        if(!dir.exists()){
            dir.mkdir();
        }
        file=new File(appUtil.getDirPath()+"/Users/UserAccount.txt");
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch (Exception e){

            }
        }
    }

    public void init(){
        try{
            inputStream=new FileInputStream(appUtil.getDirPath()+"/Users/UserAccount.txt");
            inputStreamReader=new InputStreamReader(inputStream);
            fileBufferedReader=new BufferedReader(inputStreamReader);
            String text="";
            accountList.clear();
            while ((text=fileBufferedReader.readLine())!=null){
                String[] tests=text.split("#");
                if(tests[3].equals("true")){
                    appUtil.setUserName(tests[0]);
                    appUtil.setUserPassword(tests[1]);
                    appUtil.setIsLogin(true);
                }
                Account account=new Account(tests[0],tests[1],tests[2],tests[3]);
                accountList.add(account);
            }
            fileBufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        }catch (Exception e){ }
        accountAdapter=new AccountAdapter(AccountManageActivity.this,R.layout.account_item,accountList);
        accountListView.setAdapter(accountAdapter);
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
                    jsonObject.put("name",name);
                    jsonObject.put("password",password);
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
                        Message message=accountManageHandler.obtainMessage();
                        message.what=0;
                        accountManageHandler.sendMessage(message);
                        if(resultJSON.getString("isAdmin").equals("true")){
                            appUtil.setIsAdmin(true);
                        }else {
                            appUtil.setIsAdmin(false);
                        }
                    }else {
                        Message message=accountManageHandler.obtainMessage();
                        message.what=1;
                        message.obj=resultJSON;
                        accountManageHandler.sendMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void loginSucceed(){
        try{
            inputStream=new FileInputStream(appUtil.getDirPath()+"/Users/UserAccount.txt");
            inputStreamReader=new InputStreamReader(inputStream);
            fileBufferedReader=new BufferedReader(inputStreamReader);
            String text="";
            String fileText="";
            int i=0;
            accountList.clear();
            Account account;
            while ((text=fileBufferedReader.readLine())!=null){
                if(i==0){
                    fileText=fileText + text;
                    i++;
                }else {
                    fileText=fileText + "&" + text;
                }
                String[] tests=text.split("#");
                if (tests[0].equals(userId)){
                    account=new Account(userId,name,password,"true");
                }else {
                    account=new Account(tests[0],tests[1],tests[2],"false");
                }
                accountList.add(account);
            }
            fileBufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            accountAdapter.notifyDataSetChanged();
            appUtil.setUserId(userId);
            appUtil.setUserName(name);
            appUtil.setUserPassword(password);
            appUtil.setIsLogin(true);

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
                while (j<fileTexts.length){
                    String[] fileTexts_s=fileTexts[j].split("#");
                    if (fileTexts_s[0].equals(userId)){
                        fileOutputStream.write((userId+"#"+name+"#"+password+"#true"+"\n").getBytes("utf-8"));
                    }else {
                        fileOutputStream.write((fileTexts_s[0]+"#"+fileTexts_s[1]+"#"+fileTexts_s[2]+"#false"+"\n").getBytes("utf-8"));
                    }
                    j++;
                }
            }catch (Exception e){ }finally {
                fileOutputStream.close();
            }
        }catch (Exception e){

        }
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
                    Message message=accountManageHandler.obtainMessage();
                    message.what=2;
                    accountManageHandler.sendMessage(message);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class AccountManageHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    loginSucceed();
                    break;
                case 1:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    try{
                        JSONObject reason=(JSONObject)msg.obj;
                        Toast.makeText(AccountManageActivity.this,reason.getString("reason"),Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AccountManageActivity.this,"连接超时",Toast.LENGTH_SHORT).show();
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
