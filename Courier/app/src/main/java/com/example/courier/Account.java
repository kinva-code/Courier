package com.example.courier;

public class Account {
    private String id;
    private String name;
    private String password;
    private String online_state;
    public Account(String id,String name,String password,String state){
        this.id=id;
        this.name=name;
        this.password=password;
        this.online_state=state;
    }

    public String getId(){return id;}
    public String getName(){return name;}
    public String getPassword(){return password;}
    public String getOnlineState(){return online_state;}
}
