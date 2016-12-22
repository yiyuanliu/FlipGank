package com.yiyuanliu.flipgank.fragment;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yiyuanliu.flipgank.R;
import com.yiyuanliu.flipgank.adapter.CategoryAdapter;
import com.yiyuanliu.flipgank.data.DataManager;
import com.yiyuanliu.flipgank.view.GridItemDecoration;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class CategoryFragment extends Fragment {
    private static final String TAG = "CategoryFragment";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private CategoryAdapter mCategoryAdapter;
    private Unbinder unbinder;

    public CategoryFragment() { }

    public static CategoryFragment newInstance() {
        CategoryFragment fragment = new CategoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        if (mCategoryAdapter == null) {
            mCategoryAdapter = new CategoryAdapter();
        }
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(mCategoryAdapter);
        recyclerView.addItemDecoration(new GridItemDecoration(getContext()));

        DataManager.getInstance(getContext())
                .loadCategory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onLoad, onError);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public Action1<List<String>> onLoad = new Action1<List<String>>() {
        @Override
        public void call(List<String> strings) {
            mCategoryAdapter.addCategory(strings);
        }
    };

    public Action1<Throwable> onError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            Log.e(TAG, "call: ", throwable);
        }
    };
}
