package com.yiyuanliu.flipgank;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yiyuanliu.flipgank.data.DataManager;
import com.yiyuanliu.flipgank.data.GankItem;
import com.yiyuanliu.flipgank.data.GankResponse;
import com.yiyuanliu.flipgank.view.FlipLayoutManager;
import com.yiyuanliu.flipgank.view.MySnap;
import com.yiyuanliu.flipgank.view.TestAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DataManager.Listener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new TestAdapter());
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
        dataManager.addListener(this);
        dataManager.loadNew();
        autoLoad();
    }

    private void autoLoad() {
        while (hasMore) {
            List<GankItem> gankResponses = dataManager.loadMore(day);
            if (gankResponses == null || gankResponses.size() == 0) {
                hasMore = false;
                Log.d(TAG, "no data");
            } else {
                Log.d(TAG, "load day " + gankResponses.get(0).day + " size " + gankResponses.size());
                day = gankResponses.get(0).day;
                hasMore = true;
            }
        }
    }

    private DataManager dataManager;
    private String day = null;
    private boolean hasMore = true;

    @Override
    public void onMoreLoaded() {
        Log.d(TAG, "onMoreLoaded");
        hasMore = true;
        autoLoad();
    }

    @Override
    public void onNewLoaded() {
        Log.d(TAG, "onNewLoaded");
        hasMore = true;
        day = null;
        autoLoad();
    }

    @Override
    public boolean onError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return false;
    }
}
