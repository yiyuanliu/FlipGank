package com.yiyuanliu.flipgank.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yiyuanliu.flipgank.R;
import com.yiyuanliu.flipgank.fragment.GankFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CategoryActivity extends BaseActivity {
    private static final String TAG = "CategoryActivity";

    private static final String ARG_TYPE = "type";

    public static void startActivity(Context context, String type) {
        Intent intent = new Intent(context, CategoryActivity.class);
        intent.putExtra(ARG_TYPE, type);
        context.startActivity(intent);
    }

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.scrim)
    View scrim;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.text)
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            if (getIntent() == null) {
                throw new IllegalStateException();
            }

            String type = getIntent().getExtras().getString(ARG_TYPE);

            title.setText(type);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, GankFragment.newInstance(type))
                    .commit();
        }
    }

    public void setLoading(boolean isLoading) {
        Log.d(TAG, "setLoading: " + isLoading);

        scrim.removeCallbacks(hideInfo);
        scrim.animate().cancel();
        scrim.animate().setListener(null);

        if (isLoading) {
            scrim.setVisibility(View.VISIBLE);
            loading.setVisibility(View.VISIBLE);
            text.setText("加载中...");
            scrim.setAlpha(0f);
            scrim.animate().alpha(1f).start();
        } else {
            scrim.animate().alpha(0f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scrim.setVisibility(View.INVISIBLE);
                }

                @Override public void onAnimationStart(Animator animation) { }
                @Override public void onAnimationCancel(Animator animation) { }
                @Override public void onAnimationRepeat(Animator animation) { }
            }).start();
        }
    }

    public void showInfo(String info) {
        Log.d(TAG, "showInfo: " + info);

        scrim.animate().cancel();
        scrim.removeCallbacks(hideInfo);
        scrim.animate().setListener(null);

        scrim.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        text.setText(info);
        scrim.setAlpha(0f);
        scrim.animate().alpha(1f).setDuration(230).start();
        scrim.postDelayed(hideInfo, 800);
    }

    private Runnable hideInfo = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: hideInfo");

            scrim.animate().cancel();
            scrim.animate().alpha(0f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scrim.setVisibility(View.INVISIBLE);
                }

                @Override public void onAnimationStart(Animator animation) { }
                @Override public void onAnimationCancel(Animator animation) { }
                @Override public void onAnimationRepeat(Animator animation) { }
            }).start();
        }
    };


    @OnClick(R.id.back)
    void back() {
        finish();
    }
}
