package com.example.groovemax.ACR.Adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.example.groovemax.ACR.SQLite.DataBaseHelper;

/**
 * Created by 60546 on 4/24/2016.
 */
public class SimpleItemTouchHelperCallback extends  ItemTouchHelper.Callback{

    private onMoveAndSwipedListener mAdapter;

    //将实现了onMoveAndSwipedListener接口的RecyclerViewAdapter 传进来
    public SimpleItemTouchHelperCallback(onMoveAndSwipedListener listener){
        mAdapter = listener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        //设置拖拽方向为上下
        //final int dragFlags = ItemTouchHelper.UP|ItemTouchHelper.DOWN;
        final int dragFlags = 0;
        //设置侧滑方向为从左到右和从右到左都可以
        final int swipeFlags = ItemTouchHelper.START|ItemTouchHelper.END;
        //将方向参数设置进去
        return makeMovementFlags(dragFlags,swipeFlags);
    }

    //当我们拖动item时会回调此方法
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    //当我们侧滑item时会回调此方法
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    public interface onMoveAndSwipedListener {
        boolean onItemMove(int fromPosition , int toPosition);
        void onItemDismiss(int position);
    }

}
