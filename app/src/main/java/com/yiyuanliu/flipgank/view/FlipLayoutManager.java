package com.yiyuanliu.flipgank.view;

import android.content.Context;
import android.graphics.PointF;
import android.icu.text.LocaleDisplayNames;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class FlipLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    private static final String TAG = "FlipLayoutManager";
    private static final int MIN_VY = 200;

    private int mPosition;
    private int mPositionOffset;
    private int mMinVy;

    private int mPendingPosition;

    public FlipLayoutManager(Context context) {
        mMinVy = (int) (context.getResources().getDisplayMetrics().density * MIN_VY);
    }

    public int calculateDistance(View view) {
        int pos = getPosition(view);
        final int now = getItemHeightInPositon() * mPosition + mPositionOffset;
        final int to = getItemHeightInPositon() * pos;

        return to - now;
    }

    public int findTargetPosition(int vY) {
        int ans = mPosition;

        int absV = vY > 0 ? vY : -vY;
        if (absV > mMinVy) {
            if (vY * mPositionOffset > 0) {
                int d = vY > 0 ? 1 : -1;
                ans += d;
            } else {
                ans = mPosition;
            }
        } else {
            ans = mPosition;
        }

        int count = getItemCount();
        if (count == 0) {
            return 0;
        }

        ans = Math.min(count - 1, Math.max(0, ans));
        return ans;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        if (mPendingPosition != -1) {
            mPositionOffset = 0;
            mPosition = mPendingPosition;
            mPendingPosition = -1;
        }

        fill(recycler, state);
    }

    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int count = state.getItemCount();

        checkPosition(state);

        for (int i = 0;i < count;i ++) {
            final View child = getChildAt(i);
            if (child != null) {
                int position = getPosition(child);
                if (position == mPosition
                        || (position == mPosition - 1 && mPositionOffset < 0)
                        || (position == mPosition + 1 && mPositionOffset > 0)){
                    detachAndScrapView(child, recycler);
                } else {
                    removeAndRecycleView(child, recycler);
                }
            }
        }

        View secondary = null;
        final View primary = recycler.getViewForPosition(mPosition);
        final float percent = (float)mPositionOffset / getItemHeightInPositon();

        final int next = mPositionOffset > 0 ? mPosition + 1 : mPosition - 1;
        if (mPositionOffset != 0 && next >= 0 && next < state.getItemCount()) {
            final View nextChild = recycler.getViewForPosition(next);
            addView(nextChild);
            measureChildWithMargins(nextChild, 0, 0);
            secondary = nextChild;
            layoutDecorated(secondary, 0, 0, getWidth(), getHeight());
        }

        addView(primary);
        measureChildWithMargins(primary, 0, 0);
        layoutDecorated(primary, 0, 0, getWidth(), getHeight());

        if (primary instanceof FlipCard && (secondary == null || secondary instanceof FlipCard)) {
            if (secondary == null) {
                ((FlipCard) primary).setState(true, 0);
            } else {
                ((FlipCard) secondary).setState(false, percent);
                ((FlipCard) primary).setState(true, percent);
            }
        } else {
            throw new IllegalStateException("itemView should be instance of FlipCard");
        }
    }

    private void checkPosition(RecyclerView.State state) {
        final int itemHeight = getItemHeightInPositon();
        final int total = mPosition * itemHeight + mPositionOffset;
        final int max = (state.getItemCount() - 1) * itemHeight;

        int pos = Math.max(0, Math.min(total, max));
        mPosition = Math.round(pos / (float)itemHeight);
        mPosition = mPosition >= 0 ? mPosition : 0;
        mPositionOffset = (int) (pos - mPosition * itemHeight);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int before = mPosition * getItemHeightInPositon() + mPositionOffset;
        mPositionOffset += dy;
        checkPosition(state);
        final int after = mPosition * getItemHeightInPositon() + mPositionOffset;
        final int ans = (int) (after - before);

        fill(recycler, state);

        return ans;
    }

    @Override
    public void scrollToPosition(int position) {
        mPosition = position;
        mPositionOffset = 0;
        requestLayout();
        Log.d(TAG, "scrollToPosition " + position + " position " + mPosition + " positionOffset " + mPositionOffset);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        Log.d(TAG, "smoothScrollTo " + position + " position " + mPosition + " positionOffset " + mPositionOffset);
        FlipScroller scroller = new FlipScroller(recyclerView.getContext());
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }


    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        int dir = 0;
        int now = mPosition * getItemHeightInPositon() + mPositionOffset;
        int to = targetPosition * getItemHeightInPositon();
        if (now > to) {
            dir = -1;
        } else if (now < to) {
            dir = 1;
        }

        Log.d(TAG, "computeScrollVector " + dir + " now " + mPosition + " target " + targetPosition);

        return new PointF(0, dir);
    }

    public View findSnapView() {

        for (int i = 0;i < getChildCount(); i ++ ){
            View child = getChildAt(i);
            if (getPosition(child) == mPosition) {
                return child;
            }
        }

        return null;
    }

    private int getItemHeightInPositon() {
        return getHeight() / 2;
    }

    private class FlipScroller extends LinearSmoothScroller {
        private static final String TAG = "FlipScroller";

        public FlipScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDyToMakeVisible(View view, int snapPreference) {
            final int position = getPosition(view);
            final int now = mPositionOffset + mPosition * getItemHeightInPositon();
            final int to = getPosition(view) * getItemHeightInPositon();
            Log.d(TAG, "calculateDyToMakeVisible: position " + position + " ans " + (to - now));
            return (now - to);
        }

        @Override
        public int calculateDxToMakeVisible(View view, int snapPreference) {
            return 0;
        }
    }
}
