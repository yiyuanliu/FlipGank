package com.yiyuanliu.flipgank.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yiyuanliu.flipgank.activity.BaseActivity;
import com.yiyuanliu.flipgank.activity.MainActivity;
import com.yiyuanliu.flipgank.R;
import com.yiyuanliu.flipgank.adapter.GankAdapter;
import com.yiyuanliu.flipgank.data.DataManager;
import com.yiyuanliu.flipgank.data.GankItem;
import com.yiyuanliu.flipgank.view.flipview.FlipLayoutManager;
import com.yiyuanliu.flipgank.view.flipview.FlipRefreshListener;
import com.yiyuanliu.flipgank.view.flipview.MySnap;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class GankFragment extends Fragment implements FlipRefreshListener.Listener, GankAdapter.Listener {
    private static final String TAG = "GankFragment";

    private static final String ARG_TYPE = "type";

    private String mType;
    private Unbinder unbinder;
    private GankAdapter mAdapter;
    private String mLastLoad;
    private String mLatest;
    private DataManager mDataManager;
    private Subscription mSubscription;
    private BaseActivity baseActivity;
    private boolean mIsLoading;
    private FlipRefreshListener mFlipListener;
    private boolean mHasMore = true;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_hint)
    TextView refreshHint;
    @BindView(R.id.refresh_icon)
    ImageView refreshIcon;

    public GankFragment() { }

    public static GankFragment newInstance(String type) {
        GankFragment fragment = new GankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(ARG_TYPE);
        }

        mDataManager = DataManager.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gank, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        if (mAdapter == null) {
            mAdapter = new GankAdapter(getContext(), this);
        }

        recyclerView.setAdapter(mAdapter);
        FlipLayoutManager layoutManager = new FlipLayoutManager(getContext());

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(layoutManager);
        MySnap snap = new MySnap();
        snap.attachToRecyclerView(recyclerView);
        if (mFlipListener == null) {
            mFlipListener = new FlipRefreshListener(this);
        }
        recyclerView.addOnScrollListener(mFlipListener);

        if (mAdapter.getDataCount() == 0) {
            if (mType == null)
                loadNew();
            else {
                loadMore();
            }
        } else {
            mFlipListener.onScrolled(recyclerView, 0, 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }

        mIsLoading = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            baseActivity = (BaseActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        baseActivity = null;
    }

    private void loadMore() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }

        mIsLoading = true;

        mSubscription = mDataManager.loadMore(mLastLoad)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onMore, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        // do nothing
                    }
                });
    }

    private void loadNew() {
        Log.d(TAG, "loadNew: ");

        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        mIsLoading = true;
        //延时一下，效果看起来更明显
        mSubscription = mDataManager.loadNew()
                .delay(1500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNew, onError);
    }

    private Action1<Throwable> onError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            mIsLoading = false;

            Log.e(TAG, "onError", throwable);
            if (baseActivity != null) {
                baseActivity.showInfo("加载失败");
            } else {
                Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Action1<List<GankItem>> onNew = new Action1<List<GankItem>>() {
        @Override
        public void call(List<GankItem> gankItemList) {
            mIsLoading = false;

            if (gankItemList.size() > 0) {
                mHasMore = true;
                final String latest = gankItemList.get(0).day;
                if (!latest.equals(mLatest)) {
                    mLatest = latest;
                    mLastLoad = latest;
                    mAdapter.clear();
                    if (mType != null) {
                        Iterator<GankItem> itemIterator = gankItemList.listIterator();
                        while (itemIterator.hasNext()) {
                            if (!itemIterator.next().type.equals(mType)) {
                                itemIterator.remove();
                            }
                        }
                    }
                    mAdapter.addData(gankItemList);
                    recyclerView.scrollToPosition(0);

                    if (baseActivity != null) {
                        baseActivity.setLoading(false);
                    }
                } else if (baseActivity != null) {
                    baseActivity.showInfo("已经是最新内容");
                }
            }
            mFlipListener.onScrolled(recyclerView, 0, 0);
        }
    };

    private Action1<List<GankItem>> onMore = new Action1<List<GankItem>>() {
        @Override
        public void call(List<GankItem> gankItemList) {
            mIsLoading = false;

            if (gankItemList.size() > 0) {
                mLastLoad = gankItemList.get(0).day;
                Log.d(TAG, "onMore: " + mLastLoad + new Gson().toJson(gankItemList.get(0)));
                if (mType != null && !mType.equals("like")) {
                    Iterator<GankItem> itemIterator = gankItemList.listIterator();
                    while (itemIterator.hasNext()) {
                        if (!itemIterator.next().type.equals(mType)) {
                            itemIterator.remove();
                        }
                    }
                }

                if (mType != null && mType.equals("like")) {
                    Iterator<GankItem> itemIterator = gankItemList.listIterator();
                    while (itemIterator.hasNext()) {
                        if (!itemIterator.next().like) {
                            itemIterator.remove();
                        }
                    }
                }

                mAdapter.addData(gankItemList);

                if (mLatest == null) {
                    mLatest = mLastLoad;
                }
            } else {
                mHasMore = false;
                mAdapter.setHasMore(mHasMore);
            }

            mAdapter.setHasMore(mHasMore);
            mFlipListener.onScrolled(recyclerView, 0, 0);
        }
    };

    @Override
    public void onRefresh() {
        loadNew();
        if (baseActivity != null) {
            baseActivity.setLoading(true);
        }
    }

    @Override
    public void onDrag(float percent, boolean shouldRefresh) {
        if (refreshHint != null && getView() != null) {
            if (shouldRefresh) {
                refreshHint.setText("松开可以刷新");
            } else {
                refreshHint.setText("下拉刷新页面");
            }

            View view = getView();
            int from = 0x00;
            int to = 0x30;
            int now = (from + (int)((to - from) * percent));
            int color = 0xFF << 24 | now << 16 | now << 8 | now;
            view.setBackgroundColor(color);
            refreshIcon.setRotation(percent * 360);
        }
    }

    @Override
    public void onLoadMore() {
        if (!mIsLoading && mHasMore) {
            loadMore();
        }
    }

    @Override
    public void showInfo(String info) {
        if (baseActivity != null) {
            baseActivity.showInfo(info);
        }
    }
}
