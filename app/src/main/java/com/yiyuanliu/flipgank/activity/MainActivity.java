package com.yiyuanliu.flipgank.activity;

import android.animation.Animator;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yiyuanliu.flipgank.R;
import com.yiyuanliu.flipgank.adapter.CategoryAdapter;
import com.yiyuanliu.flipgank.data.GankItem;
import com.yiyuanliu.flipgank.fragment.AboutFragment;
import com.yiyuanliu.flipgank.fragment.CategoryFragment;
import com.yiyuanliu.flipgank.fragment.GankFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.scrim)
    View scrim;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.text)
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.icon_main);
        tabLayout.getTabAt(1).setIcon(R.drawable.icon_category);
        tabLayout.getTabAt(2).setIcon(R.drawable.icon_about);
    }

    private PagerAdapter mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                return CategoryFragment.newInstance();
            } else if (position == 2) {
                return AboutFragment.newInstance();
            }
            return GankFragment.newInstance(null);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    };

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
        scrim.setAlpha(1f);
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

}
