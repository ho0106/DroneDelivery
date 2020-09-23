package com.example.dronedelivery;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class DroneLog extends RecyclerView.Adapter<DroneLog.DroneLogViewHolder> {

    private ArrayList<String> mDroneLog;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class DroneLogViewHolder extends RecyclerView.ViewHolder {

        protected TextView droneTextView;

        DroneLogViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            this.droneTextView = (TextView) itemView.findViewById(R.id.stateLog);
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public DroneLog(ArrayList<String> list) {
        this.mDroneLog = list ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public DroneLog.DroneLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drone_recyclerview_items,parent,false);
        DroneLogViewHolder droneLogViewHolder = new DroneLogViewHolder(view);
        return droneLogViewHolder;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull DroneLog.DroneLogViewHolder viewholder, int position) {
        viewholder.droneTextView.setText(mDroneLog.get(position));

        String text = mDroneLog.get(position);

        if (text.contains(" ※ ")) {
            viewholder.droneTextView.setTextColor(Color.RED);
        } else {
            viewholder.droneTextView.setTextColor(Color.WHITE);
        }
        viewholder.droneTextView.setText(text);
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return  mDroneLog.size();
    }
}