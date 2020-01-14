package com.example.courier;

import android.app.Application;

public class ApplicationUtil extends Application {
    private static String userId="";
    private static String userName="";
    private static String userPassword="";
    private static boolean isLogin=false;
    private static boolean isAdmin=false;
    private static String dirPath;
    private static String ServerUrl="http://192.168.43.92:8080/Courier";

    public void setUserId(String id){this.userId=id;}
    public void setUserName(String name){
        userName=name;
    }
    public void setUserPassword(String password){
        userPassword=password;
    }
    public void setIsLogin(boolean isLogin){this.isLogin=isLogin;}
    public void setIsAdmin(boolean isAdmin){this.isAdmin=isAdmin;}
    public void setDirPath(String path){dirPath=path;}

    public String getUserId(){return userId;}
    public String getUserName(){return userName;}
    public String getUserPassword(){return userPassword;}
    public boolean isLogin(){return isLogin;}
    public boolean isAdmin(){return isAdmin;}
    public String getDirPath(){return dirPath;}
    public String getServerUrl(){return ServerUrl;}
}
