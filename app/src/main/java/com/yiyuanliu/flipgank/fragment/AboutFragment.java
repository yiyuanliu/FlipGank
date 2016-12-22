package com.yiyuanliu.flipgank.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yiyuanliu.flipgank.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AboutFragment extends Fragment {
    private static final String TAG = "AboutFragment";

    @BindView(R.id.image_github)
    ImageView github;
    @BindView(R.id.image_write)
    ImageView write;
    private Unbinder unbinder;

    public AboutFragment() { }

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        Glide.with(this)
                .load(R.drawable.image_github)
                .centerCrop()
                .into(github);

        Glide.with(this)
                .load(R.drawable.image_write)
                .centerCrop()
                .into(write);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.click_email)
    protected void email() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","yiyuanliu1997@gmail.com", null));
        startActivity(Intent.createChooser(intent, "Send Email to yiyuanliu1997@gmail.com"));
    }

    @OnClick(R.id.click_github)
    protected void github() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yiyuanliu/FlipGank"));
        startActivity(intent);
    }
}
