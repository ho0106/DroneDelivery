package com.example.dronedelivery;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class DroneLog extends RecyclerView.Adapter<DroneLog.ViewHolder> {

    private ArrayList<String> mData;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView textView1 ;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            this.textView1 = (TextView) itemView.findViewById(R.id.stateLog);
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public DroneLog(ArrayList<String> list) {
        this.mData = list ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public DroneLog.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_items,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull DroneLog.ViewHolder viewholder, int position) {
        viewholder.textView1.setText(mData.get(position));

        String text = mData.get(position);

        if (text.contains(" ※ ")) {
            viewholder.textView1.setTextColor(Color.RED);
        } else {
            viewholder.textView1.setTextColor(Color.WHITE);
        }
        viewholder.textView1.setText(text);
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return  mData.size();
    }
}