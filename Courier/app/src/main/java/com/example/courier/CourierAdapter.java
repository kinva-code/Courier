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

public class CourierAdapter extends RecyclerView.Adapter<CourierAdapter.ViewHolder>  {
    private List<Courier> courierList;
    private Context context;
    private ApplicationUtil appUtil=new ApplicationUtil();
    class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout courierLayout;
        TextView courierName;
        public ViewHolder(View view){
            super(view);
            courierLayout=(LinearLayout)view.findViewById(R.id.courier_layout);
            courierName=(TextView)view.findViewById(R.id.courier_name);
        }
    }

    public CourierAdapter(List<Courier> courierList,Context context){
        this.courierList=courierList;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        final View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.courier_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.courierLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                Courier courier=courierList.get(position);
                Intent intent=new Intent(context,AdminAddressActivity.class);
                intent.putExtra("courier_id",courier.getId());
                intent.putExtra("name",courier.getName());
                context.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        Courier courier=courierList.get(position);
        holder.courierName.setText(courier.getName());
    }

    @Override
    public int getItemCount(){return courierList.size();}
}
