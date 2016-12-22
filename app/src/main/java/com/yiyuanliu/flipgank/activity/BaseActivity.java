package com.yiyuanliu.flipgank.activity;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by YiyuanLiu on 2016/12/22.
 */

public abstract class BaseActivity extends AppCompatActivity {
    public abstract void showInfo(String info);
    public abstract void setLoading(boolean isLoading);
}
