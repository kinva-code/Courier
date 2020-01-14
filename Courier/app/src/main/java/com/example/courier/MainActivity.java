package com.example.courier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.courier.zxing.android.CaptureActivity;
import com.google.android.material.tabs.TabLayout;
import com.makeramen.roundedimageview.RoundedImageView;

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

public class MainActivity extends AppCompatActivity {
    private View viewpaper_news,viewpaper_mine;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private List<View> viewList;

    //viewpaper_news
    private ImageView admin;
    private ImageView scan;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView orderRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList=new ArrayList<>();

    //viewpaper_mine
    private RoundedImageView accountHead;
    private TextView userName;
    private LinearLayout accountManage;
    private LinearLayout accountMessage;
    private LinearLayout aboutHelp;

    private String dirpath;
    private File file;
    private FileOutputStream fileOutputStream;
    private FileInputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader fileBufferedReader;
    private ApplicationUtil appUtil=new ApplicationUtil();
    private HttpURLConnection urlConnection=null;
    private ProgressDialog progressDialog=null;
    private boolean timerClose;
    private MainActivityHandler mainActivityHandler;

    private int isFirst=0;
    private String textAccount;
    private String textPassword;

    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final int REQUEST_CODE_SCAN = 0x0000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();     //隐藏标题栏
        setStatusBar();                   //状态栏透明
        viewPager = (ViewPager) findViewById(R.id.viewpaper);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        final LayoutInflater inflater = getLayoutInflater();
        viewpaper_news=inflater.inflate(R.layout.viewpaper_news,null);
        viewpaper_mine=inflater.inflate(R.layout.viewpaper_mine,null);
        viewList=new ArrayList<View>();
        viewList.add(viewpaper_news);
        viewList.add(viewpaper_mine);
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                // TODO Auto-generated method stub
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // TODO Auto-generated method stub
                container.addView(viewList.get(position));
                return viewList.get(position);
            }
        };
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout)); //viewPager与tabLayout联动，滑动view可切换tab，但点击tab不会滑动view
        viewPager.setAdapter(pagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {   //viewPager与tabLayout联动，点击tab可滑动view，完成两者相互联动
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        inflate();
        mainActivityHandler=new MainActivityHandler();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initOrder();
                new timer().start();
            }
        });

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        orderRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this,R.drawable.shape_divider));
        orderRecyclerView.addItemDecoration(dividerItemDecoration);
        orderAdapter=new OrderAdapter(orderList,MainActivity.this);
        orderRecyclerView.setAdapter(orderAdapter);

        setOnClickListener();

        createFile();
        try{
            inputStream=new FileInputStream(dirpath+"/Users/UserAccount.txt");
            inputStreamReader=new InputStreamReader(inputStream);
            fileBufferedReader=new BufferedReader(inputStreamReader);
            String text="";
            boolean findLogin=false;
            while ((text=fileBufferedReader.readLine())!=null){
                Log.e("Main",text);
                String[] texts=text.split("#");
                if(texts[3].equals("true")){
                    findLogin=true;
                    textAccount=texts[1];
                    textPassword=texts[2];
                    progressDialog= ProgressDialog.show(MainActivity.this,"logining...","please wait...");
                    startLogin();
                    new timer().start();
                    break;
                }
            }
            if(!findLogin){
                userName.setText("未登录");
                Toast.makeText(MainActivity.this,"请先登录",Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        isFirst++;
    }

    public void inflate(){
        swipeRefreshLayout=(SwipeRefreshLayout)viewpaper_news.findViewById(R.id.courier_swipe);
        orderRecyclerView=(RecyclerView)viewpaper_news.findViewById(R.id.order_recycle_view);
        accountHead=(RoundedImageView)viewpaper_mine.findViewById(R.id.account_head);
        accountManage=(LinearLayout)viewpaper_mine.findViewById(R.id.account_manage);
        accountMessage=(LinearLayout)viewpaper_mine.findViewById(R.id.account_message);
        aboutHelp=(LinearLayout)viewpaper_mine.findViewById(R.id.about_help);
        userName=(TextView)viewpaper_mine.findViewById(R.id.user_name);
        admin=(ImageView) viewpaper_news.findViewById(R.id.admin);
        scan=(ImageView)viewpaper_news.findViewById(R.id.scan);
    }

    public void setOnClickListener(){
        accountHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        accountManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,AccountManageActivity.class);
                startActivity(intent);
            }
        });
        accountMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if(appUtil.isLogin()){
                    intent=new Intent(MainActivity.this,AccountMessageActivity.class);
                }else {
                    intent=new Intent(MainActivity.this,LoginActivity.class);
                }
                startActivity(intent);
            }
        });
        aboutHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,AboutHelpActivity.class);
                startActivity(intent);
            }
        });
        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if(appUtil.isLogin()){
                    if(appUtil.isAdmin()){
                        intent=new Intent(MainActivity.this,AdminCourierActivity.class);
                        startActivity(intent);
                    }else {
                        Toast.makeText(MainActivity.this,"您不是管理员，无法进入",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    intent=new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                }

            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //动态权限申请
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    goScan();
                }
            }
        });
    }

    /**
     * 跳转到扫码界面扫码
     */
    private void goScan(){
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goScan();
                } else {
                    Toast.makeText(this, "你拒绝了权限申请，可能无法打开相机扫码哟！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                //返回的文本内容
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                //返回的BitMap图像
                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);

                Log.e("scan",content);
                String string=new String(Base64.decode(content.getBytes(),Base64.DEFAULT));
                try {
                    JSONObject jsonObject=new JSONObject(URLDecoder.decode(string,"utf-8"));
                    Intent intent=new Intent(MainActivity.this,UserInfoActivity.class);
                    intent.putExtra("name",jsonObject.getString("name"));
                    intent.putExtra("sex",jsonObject.getString("sex"));
                    intent.putExtra("age",jsonObject.getString("age"));
                    intent.putExtra("phone_num",jsonObject.getString("phone_num"));
                    intent.putExtra("city",jsonObject.getString("city"));
                    intent.putExtra("address",jsonObject.getString("address"));
                    startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e("appUtil",String.valueOf(appUtil.isAdmin()));
        createFile();
        if(isFirst==2){
            try{
                inputStream=new FileInputStream(dirpath+"/Users/UserAccount.txt");
                inputStreamReader=new InputStreamReader(inputStream);
                fileBufferedReader=new BufferedReader(inputStreamReader);
                String text="";
                while ((text=fileBufferedReader.readLine())!=null){
                    Log.e("Main",text);
                    String[] texts=text.split("#");
                    if(texts[3].equals("true")){
                        appUtil.setUserId(texts[0]);
                        appUtil.setUserName(texts[1]);
                        appUtil.setUserPassword(texts[2]);
                        appUtil.setIsLogin(true);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            if(appUtil.isLogin()){
                userName.setText(appUtil.getUserName());
                progressDialog= ProgressDialog.show(MainActivity.this,"logining...","please wait...");
                initOrder();
                new timer().start();
            }else {
                userName.setText("未登录");
                orderList.clear();
                orderAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this,"请先登录",Toast.LENGTH_SHORT).show();
            }
        }else {
            isFirst++;
        }
    }

    public void createFile(){
        dirpath=this.getFilesDir().toString();
        appUtil.setDirPath(dirpath);
        File dir=new File(dirpath+"/Users");
        if(!dir.exists()){
            dir.mkdir();
        }
        file=new File(dirpath+"/Users/UserAccount.txt");
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch (Exception e){

            }
        }
    }

    public void initOrder(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                orderList.clear();
                orderAdapter.notifyDataSetChanged();
                String strUrl=appUtil.getServerUrl()+"/InitOrderServlet";
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
                    jsonObject.put("isLogin",String.valueOf(appUtil.isLogin()));
                    jsonObject.put("user_id",appUtil.getUserId());
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
                        Message message=mainActivityHandler.obtainMessage();
                        message.what=0;
                        message.obj=resultJSON;
                        mainActivityHandler.sendMessage(message);
                    }else {
                        Message message=mainActivityHandler.obtainMessage();
                        message.what=1;
                        message.obj=resultJSON;
                        mainActivityHandler.sendMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
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
                    jsonObject.put("name",textAccount);
                    jsonObject.put("password",textPassword);
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
                        Message message=mainActivityHandler.obtainMessage();
                        message.what=3;
                        message.obj=resultJSON;
                        mainActivityHandler.sendMessage(message);
                    }else {
                        Message message=mainActivityHandler.obtainMessage();
                        message.what=4;
                        message.obj=resultJSON;
                        mainActivityHandler.sendMessage(message);
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
        userName.setText(appUtil.getUserName());
        progressDialog= ProgressDialog.show(MainActivity.this,"logining...","please wait...");
        initOrder();
        new timer().start();
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
                    Message message=mainActivityHandler.obtainMessage();
                    message.what=2;
                    mainActivityHandler.sendMessage(message);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class MainActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    timerClose=true;
                    try{
                        JSONObject resultJSON=(JSONObject)msg.obj;
                        int length=resultJSON.getInt("length");
                        Log.e("length",String.valueOf(length));
                        orderList.clear();
                        for(int i=length-1;i>=0;i--){
                            String content=resultJSON.getString("order"+i);
                            String[] contents=content.split("#");
                            Order order=new Order(contents[0],contents[1],contents[2],contents[3],contents[4],contents[5],contents[6],contents[7],contents[8],contents[9]);
                            orderList.add(order);
                        }
                        orderAdapter.notifyDataSetChanged();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    timerClose=true;
                    try{
                        JSONObject reason=(JSONObject)msg.obj;
                        Toast.makeText(MainActivity.this,reason.getString("reason"),Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this,"连接超时",Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    try{
                        JSONObject result=(JSONObject)msg.obj;
                        loginSucceed(result.getString("id"));
                        if(result.getString("isAdmin").equals("true")){
                            appUtil.setIsAdmin(true);
                        }else {
                            appUtil.setIsAdmin(false);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    timerClose=true;
                    try{
                        JSONObject reason=(JSONObject)msg.obj;
                        Toast.makeText(MainActivity.this,reason.getString("reason"),Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    userName.setText("未登录");
                    orderList.clear();
                    orderAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this,"请先登录",Toast.LENGTH_SHORT).show();
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
