package com.yiyuanliu.flipgank.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.yiyuanliu.flipgank.R;
import com.yiyuanliu.flipgank.adapter.GankAdapter;
import com.yiyuanliu.flipgank.data.GankItem;
import com.yiyuanliu.flipgank.view.GankBottom;
import com.yiyuanliu.flipgank.view.GankItemView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GankViewActivity extends BaseActivity {
    private static final String ARG_GANK = "gank";

    public static void startActivity(Context context, GankItem gankItem) {
        Intent intent = new Intent(context, GankViewActivity.class);
        intent.putExtra(ARG_GANK, gankItem);
        context.startActivity(intent);
    }

    @BindView(R.id.webview)
    WebView mWebView;
    @BindView(R.id.like)
    ImageButton likeButton;
    @BindView(R.id.loading)
    ProgressBar loading;

    GankItem mGankItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gank_view);

        ButterKnife.bind(this);

        if (getIntent() == null) {
            throw new IllegalStateException();
        }
        mGankItem = (GankItem) getIntent().getExtras().getSerializable(ARG_GANK);

        mWebView.loadUrl(mGankItem.url);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                setLoading(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setLoading(false);
            }
        });

        if (mGankItem.like) {
            likeButton.setImageResource(R.drawable.bt_like);
        } else {
            likeButton.setImageResource(R.drawable.bt_unlike);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.back)
    void close(View view) {
        finish();
    }

    @OnClick(R.id.share)
    void share(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mGankItem.desc + "\n" + mGankItem.url);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "分享链接"));
    }

    @OnClick(R.id.open_in_browser)
    void open(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mGankItem.url));
        startActivity(intent);
    }

    @OnClick(R.id.like)
    void like(View view) {
        mGankItem.like = !mGankItem.like;
        if (mGankItem.like) {
            likeButton.setImageResource(R.drawable.bt_like);
        } else {
            likeButton.setImageResource(R.drawable.bt_unlike);
        }
    }

    @OnClick(R.id.more)
    void showMore(View view) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        GankBottom gankBottom = (GankBottom) LayoutInflater.from(this).inflate(R.layout.bottom_sheet, null, false);
        gankBottom.bind(mGankItem, null);
        bottomSheetDialog.setContentView( gankBottom);
        bottomSheetDialog.show();
    }

    @Override
    public void showInfo(String info) { }

    @Override
    public void setLoading(boolean isLoading) {
        loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
