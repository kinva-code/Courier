package com.example.courier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Hashtable;

import android.util.Base64;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class ViewQRcodeActivity extends AppCompatActivity {
    private ImageView back;
    private ImageView QRcode;
    private ViewQRcodeHandler viewQRcodeHandler;
    private Bitmap bitmap;

    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_qrcode);
        getSupportActionBar().hide();     //隐藏标题栏
        setStatusBar();                   //状态栏透明
        back=(ImageView)findViewById(R.id.back);
        QRcode=(ImageView)findViewById(R.id.QRcode);
        viewQRcodeHandler=new ViewQRcodeHandler();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        QRcode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View contentView= LayoutInflater.from(ViewQRcodeActivity.this).inflate(R.layout.scan_qrcode,null);
                popupWindow=new PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,true);
                popupWindow.setContentView(contentView);
                final TextView scan=(TextView)contentView.findViewById(R.id.scan);
                TextView cancel=(TextView)contentView.findViewById(R.id.cancel);
                scan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("erroe","1111111111");
                        String result=new QRHelper().getResult(bitmap);
                        Log.e("erroe","222222222");
                        Log.e("erroe",result+"");
                        if(result!=null){
                            String string=new String(Base64.decode(result.getBytes(),Base64.DEFAULT));
                            try {
                                JSONObject jsonObject=new JSONObject(URLDecoder.decode(string,"utf-8"));
                                Intent intent=new Intent(ViewQRcodeActivity.this,UserInfoActivity.class);
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
                        }else {
                            Toast.makeText(ViewQRcodeActivity.this,"二维码识别失败",Toast.LENGTH_SHORT).show();
                        }
                        popupWindow.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                View rootView=LayoutInflater.from(ViewQRcodeActivity.this).inflate(R.layout.activity_registe,null);
                popupWindow.showAtLocation(rootView, Gravity.BOTTOM,0,0);
                return true;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String path="http://192.168.43.92:8080/Courier/QRcode/"+getIntent().getStringExtra("user_id")+".png";
                    URL url=new URL(path);
                    HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.connect();
                    if(connection.getResponseCode()==200){
                        InputStream is=connection.getInputStream();
                        Bitmap bm= BitmapFactory.decodeStream(is);
                        Message message=new Message();
                        message.obj=bm;
                        viewQRcodeHandler.sendMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class ViewQRcodeHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            QRcode.setImageBitmap((Bitmap)msg.obj);
            bitmap=(Bitmap)msg.obj;
        }
    }

    private class QRHelper{
        public String getResult(Bitmap bitmap){
            String string=null;
            if(bitmap!=null){
                string=scanBitmap(bitmap);
            }
            if(!TextUtils.isEmpty(string)){
                return string;
            }
            return null;
        }

        private String scanBitmap(Bitmap bitmap){
            Result result=scan(bitmap);
            if(result!=null){
                return recode(result.getText());
            }else {
                return null;
            }
        }

        private String recode(String str){
            String format="";
            try{
                boolean ISO= Charset.forName("ISO-8859-1").newEncoder().canEncode(str);
                if(ISO){
                    format=new String(str.getBytes("ISO-8859-1"),"GB2312");
                }else {
                    format=str;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return format;
        }

        private com.google.zxing.Result scan(Bitmap bitmap){
            Hashtable<DecodeHintType,String> hints=new Hashtable<DecodeHintType, String>();
            hints.put(DecodeHintType.CHARACTER_SET,"utf-8");
            Bitmap scanBitmap=Bitmap.createBitmap(bitmap);
            int px[]=new int[scanBitmap.getWidth() * scanBitmap.getHeight()];
            scanBitmap.getPixels(px,0,scanBitmap.getWidth(),0,0,scanBitmap.getWidth(),scanBitmap.getHeight());
            RGBLuminanceSource source=new RGBLuminanceSource(scanBitmap.getWidth(),scanBitmap.getHeight(),px);
            BinaryBitmap tempBitmap=new BinaryBitmap(new HybridBinarizer(source));
            QRCodeReader reader=new QRCodeReader();
            try{
                return reader.decode(tempBitmap,hints);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
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
