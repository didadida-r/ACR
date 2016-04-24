package com.example.groovemax.ACR.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.groovemax.ACR.R;
import com.example.groovemax.ACR.SQLite.DataBaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Created by 60546 on 4/24/2016.
 */


/*
 * RecyclerView的Adapter
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements SimpleItemTouchHelperCallback.onMoveAndSwipedListener {

    private Context context;
    private DataBaseHelper helper;
    private ArrayList<Map<String, Object>> mDate;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        //交换mItems数据的位置
        Collections.swap(mDate, fromPosition, toPosition);
        //交换RecyclerView列表中item的位置
        notifyItemMoved(fromPosition,toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        if(helper == null)
            helper = new DataBaseHelper(context, "cmd", 1);
        helper.getReadableDatabase().delete("cmd_table", "cmd_content=?",
                new String[]{mDate.get(position).get("pinyin").toString()});
        //删除mItems数据
        mDate.remove(position);
        //删除RecyclerView列表对应item
        notifyItemRemoved(position);
    }

    public interface OnRecyclerViewItemClickListener{
        void onItemClick(View view, int position);
    }

    public MyAdapter(Context context, ArrayList<Map<String, Object>> data){
        mDate = data;
        this.context = context;
    }

    public void setmOnItemClickListener(OnRecyclerViewItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.recycler_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, (int)v.getTag());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //根据position给每个item设置一个标签，方便取出
        holder.itemView.setTag(position);
        if(mDate != null)
            holder.textView.setText(mDate.get(position).get("pinyin").toString());
    }

    @Override
    public int getItemCount() {
        if(mDate == null)
            return 0;
        return mDate.size();
    }



    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public MyViewHolder(View view){
            super(view);
            textView = (TextView) view.findViewById(R.id.textView);
        }
    }
}