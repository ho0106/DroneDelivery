package com.example.dronedelivery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class OrderLog extends RecyclerView.Adapter<OrderLog.OrderLogViewHolder> {

    private ArrayList<String> mOrderLog;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class OrderLogViewHolder extends RecyclerView.ViewHolder {

        protected TextView orderTextView;

        OrderLogViewHolder(final View itemView) {
            super(itemView) ;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(v, pos);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION)
                    {
                        mLongListener.onItemLongClick(v, pos);
                    }
                    return true;
                }
            });

            // 뷰 객체에 대한 참조. (hold strong reference)
            this.orderTextView = (TextView) itemView.findViewById(R.id.orderList);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View v, int pos);
    }

    private OnItemClickListener mListener = null;
    private OnItemLongClickListener mLongListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mLongListener = listener;
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public OrderLog(ArrayList<String> list) {
        this.mOrderLog = list ;
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
        viewholder.orderTextView.setText(mOrderLog.get(position));

        String text = mOrderLog.get(position);
        viewholder.orderTextView.setText(text);
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return  (null != mOrderLog ? mOrderLog.size() : 0);
    }
}