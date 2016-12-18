package com.yiyuanliu.flipgank;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.yiyuanliu.flipgank.adapter.MainAdapter;
import com.yiyuanliu.flipgank.data.DataManager;
import com.yiyuanliu.flipgank.data.GankItem;
import com.yiyuanliu.flipgank.view.FlipLayoutManager;
import com.yiyuanliu.flipgank.view.MySnap;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new FlipLayoutManager(this));
        MySnap mySnap = new MySnap();
        mySnap.attachToRecyclerView(recyclerView);

        Button back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(3);
            }
        });

        dataManager = DataManager.getInstance(this);
        dataManager.loadMore(lastLoad)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onMore);
    }

    private DataManager dataManager;
    private String lastLoad = null;
    private boolean hasMore = true;

    private Action1<List<GankItem>> onMore = new Action1<List<GankItem>>() {
        @Override
        public void call(List<GankItem> gankItemList) {
            Log.d(TAG, "onMore " + gankItemList.size());

            MainAdapter mainAdapter = new MainAdapter();
            mainAdapter.addData(gankItemList);
            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            recyclerView.setAdapter(mainAdapter);
            recyclerView.setLayoutManager(new FlipLayoutManager(MainActivity.this));
        }
    };
}
