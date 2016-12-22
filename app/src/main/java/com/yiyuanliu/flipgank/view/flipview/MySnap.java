package com.yiyuanliu.flipgank.view.flipview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;

/**
 * Created by YiyuanLiu on 2016/12/15.
 */

public class MySnap extends SnapHelper {
    private static final String TAG = "MySnap";

    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        if (layoutManager instanceof FlipLayoutManager) {
            return new int[]{0, ((FlipLayoutManager) layoutManager).calculateDistance(targetView)};
        } else {
            throw new RuntimeException();
        }
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        FlipLayoutManager flipLayoutManager = (FlipLayoutManager) layoutManager;

        return flipLayoutManager.findSnapView();
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        if (layoutManager instanceof FlipLayoutManager) {
            return ((FlipLayoutManager) layoutManager).findTargetPosition(velocityY);
        } else {
            throw new RuntimeException();
        }
    }
}
