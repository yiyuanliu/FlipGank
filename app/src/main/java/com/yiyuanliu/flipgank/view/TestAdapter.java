package com.yiyuanliu.flipgank.view;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yiyuanliu.flipgank.R;

/**
 * Created by YiyuanLiu on 2016/12/15.
 */

public class TestAdapter extends RecyclerView.Adapter {
    private int[] colors = new int[]{
            0xff330000,
            0xff660000,
            0xff990000,
            0xffcc0000,
            0xff003300,
            0xff006600,
            0xff009900,
            0xff00cc00,
            0xff000033,
            0xff000066,
            0xff000099,
            0xff0000cc,
    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test, parent, false);
        return new RecyclerView.ViewHolder(view) { };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.image);
        imageView.setBackgroundColor(colors[position]);
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }

}
