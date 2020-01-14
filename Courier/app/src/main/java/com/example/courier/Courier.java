package com.example.courier;

public class Courier {
    private String id;
    private String name;
    public Courier(String id,String name){
        this.id=id;
        this.name=name;
    }

    public String getId(){return id;}
    public String getName(){return name;}
}
