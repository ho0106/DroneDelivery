package com.example.dronedelivery;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class OrderLog extends RecyclerView.Adapter<OrderLog.OrderLogViewHolder> {

    private ArrayList<OrderData> mOrderLog;
    private Activity context = null;

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public OrderLog(Activity context, ArrayList<OrderData> list) {
        this.context = context;
        this.mOrderLog = list ;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    class OrderLogViewHolder extends RecyclerView.ViewHolder {

        protected TextView id;
        protected TextView address;

        public OrderLogViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            this.id = (TextView) itemView.findViewById(R.id.orderID);
            this.address = (TextView) itemView.findViewById(R.id.orderAddress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(v, pos);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public OrderLog.OrderLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_recyclerview_items,parent,false);
        OrderLogViewHolder orderLogViewHolder = new OrderLogViewHolder(view);
        return orderLogViewHolder;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull OrderLog.OrderLogViewHolder viewholder, int position) {
        viewholder.id.setText(mOrderLog.get(position).getOrder_id());
        viewholder.address.setText(mOrderLog.get(position).getOrder_address());
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return  (null != mOrderLog ? mOrderLog.size() : 0);
    }
}