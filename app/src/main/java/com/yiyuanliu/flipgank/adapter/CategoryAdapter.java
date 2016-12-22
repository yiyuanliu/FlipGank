package com.yiyuanliu.flipgank.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yiyuanliu.flipgank.R;
import com.yiyuanliu.flipgank.activity.CategoryActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by YiyuanLiu on 2016/12/21.
 */

public class CategoryAdapter extends RecyclerView.Adapter {
    private List<String> mCategoryList;

    public CategoryAdapter() {
        mCategoryList = new ArrayList<>();
        mCategoryList.add("Android");
        mCategoryList.add("iOS");
        mCategoryList.add("App");
        mCategoryList.add("拓展资源");
        mCategoryList.add("瞎推荐");
        mCategoryList.add("前端");
        mCategoryList.add("休息视频");
        Collections.sort(mCategoryList);
    }

    public void addCategory(List<String> categoryList) {
        for (String category: categoryList) {
            if (category.equals("福利")) {
                continue;
            }

            boolean has = false;
            for (String item: mCategoryList) {
                if (category.equals(item)){
                    has = true;
                    break;
                }
            }

            if (!has) {
                mCategoryList.add(category);
            }
        }

        Collections.sort(mCategoryList);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryVh(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((CategoryVh)holder).bind(mCategoryList.get(position));
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

    class CategoryVh extends RecyclerView.ViewHolder {

        @BindView(R.id.category)
        TextView textView;
        @BindView(R.id.image)
        ImageView imageView;
        String category;

        public CategoryVh(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(String category) {
            this.category = category;
            this.textView.setText(category);
            if (getResId(category) != 0) {
                Glide.with(itemView.getContext())
                        .load(getResId(category))
                        .into(imageView);
            }
        }

        @OnClick(R.id.click)
        void onClick() {
            CategoryActivity.startActivity(itemView.getContext(), category);
        }
    }

    private static final int getResId(String category) {
        switch (category) {
            case "Android": return R.drawable.android;
            case "iOS": return R.drawable.ios;
            case "App": return R.drawable.app;
            case "休息视频": return R.drawable.rest;
            case "拓展资源": return R.drawable.more;
            case "前端": return R.drawable.javascript;
            case "瞎推荐": return R.drawable.recommend;
            default: return 0;
        }
    }
}
