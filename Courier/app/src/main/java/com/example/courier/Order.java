package com.example.courier;

public class Order {
    private String orderId;
    private String userId;
    private String userName;
    private String tradeName;
    private String tradeNum;
    private String tradePrice;
    private String orderPrice;
    private String createTime;
    private String deliveryTime;
    private String address;

    public Order(String orderId,String userId,String userName,String tradeName,String tradeNum,String tradePrice,String orderPrice,String createTime,String deliveryTime,String address){
        this.orderId=orderId;
        this.userId=userId;
        this.userName=userName;
        this.tradeName=tradeName;
        this.tradeNum=tradeNum;
        this.tradePrice=tradePrice;
        this.orderPrice=orderPrice;
        this.createTime=createTime;
        this.deliveryTime=deliveryTime;
        this.address=address;
    }

    public String getOrderId(){return orderId;}
    public String getUserId(){return userId;}
    public String getUserName(){return userName;}
    public String getTradeName(){return tradeName;}
    public String getTradeNum(){return tradeNum;}
    public String getTradePrice(){return tradePrice;}
    public String getOrderPrice(){return orderPrice;}
    public String getCreateTime(){return createTime;}
    public String getDeliveryTime(){return deliveryTime;}
    public String getAddress(){return address;}
}
