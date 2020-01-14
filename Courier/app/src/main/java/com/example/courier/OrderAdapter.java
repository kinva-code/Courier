package com.example.courier;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<Order> orderList;
    private Context context;
    private ApplicationUtil appUtil=new ApplicationUtil();
    class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout orderLayout;
        TextView orderId;
        TextView tradeName;
        TextView tradeNum;
        TextView tradePrice;
        TextView orderPrice;
        TextView createTime;
        TextView deliveryTime;
        TextView address;
        public ViewHolder(View view){
            super(view);
            orderLayout=(LinearLayout)view.findViewById(R.id.order_layout);
            orderId=(TextView)view.findViewById(R.id.order_id);
            tradeName=(TextView)view.findViewById(R.id.trade_name);
            tradeNum=(TextView)view.findViewById(R.id.trade_num);
            tradePrice=(TextView)view.findViewById(R.id.trade_price);
            orderPrice=(TextView)view.findViewById(R.id.order_price);
            createTime=(TextView)view.findViewById(R.id.create_time);
            deliveryTime=(TextView)view.findViewById(R.id.delivery_time);
            address=(TextView)view.findViewById(R.id.address);
        }
    }

    public OrderAdapter(List<Order> orderList,Context context){
        this.orderList=orderList;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        final View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.orderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                Order order=orderList.get(position);
                Intent intent;
                if(appUtil.isLogin()){
                    intent=new Intent(context,ViewQRcodeActivity.class);
                    intent.putExtra("user_id",order.getUserId());
                    Log.e("user_id",order.getUserId());
                }else {
                    intent=new Intent(context,LoginActivity.class);
                }
                context.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        Order order=orderList.get(position);
        holder.orderId.setText(order.getOrderId());
        holder.tradeName.setText(order.getTradeName());
        holder.tradeNum.setText("x"+order.getTradeNum());
        holder.tradePrice.setText(order.getTradePrice());
        holder.orderPrice.setText(order.getOrderPrice());
        holder.createTime.setText(order.getCreateTime());
        holder.deliveryTime.setText(order.getDeliveryTime());
        holder.address.setText(order.getAddress());
    }

    @Override
    public int getItemCount(){return orderList.size();}
}
