package com.example.courier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AccountAdapter extends ArrayAdapter<Account> {
    private int resourceId;
    public AccountAdapter(Context context, int textViewResourceId, List<Account> list){
        super(context,textViewResourceId,list);
        this.resourceId=textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Account account=getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            view= LayoutInflater.from(getContext()).inflate(resourceId,null);
            viewHolder=new ViewHolder();
            viewHolder.account_name=(TextView)view.findViewById(R.id.account_name);
            viewHolder.online_state=(TextView)view.findViewById(R.id.online_state);
            view.setTag(viewHolder);
        }else {
            view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.account_name.setText(account.getName());
        if(account.getOnlineState().equals("true")){
            viewHolder.online_state.setVisibility(View.VISIBLE);
        }else {
            viewHolder.online_state.setVisibility(View.GONE);
        }
        return view;
    }

    class ViewHolder{
        TextView account_name;
        TextView online_state;
    }
}
