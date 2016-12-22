package com.yiyuanliu.flipgank.view.flipview;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by YiyuanLiu on 2016/12/15.
 */

public class FlipCard extends FrameLayout {

    private static final String TAG = "FlipCard";

    private Paint mScrimPaint;

    private Camera mCamera;
    private Matrix mMatrix;

    private boolean mIsForground;
    private float mPercent;

    public FlipCard(Context context) {
        this(context, null);
    }

    public FlipCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlipCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScrimPaint = new Paint();
        mCamera = new Camera();
        mMatrix = new Matrix();
    }

    public void setState(boolean isForground, float percent) {
        mIsForground = isForground;
        mPercent = percent;
    }

    @Override
    public void draw(Canvas canvas) {

        if (mPercent == 0) {
            super.draw(canvas);
            return;
        }

        final int height = canvas.getHeight();
        final int width = canvas.getWidth();
        final float percent = mPercent > 0 ? mPercent : -mPercent;

        if (mIsForground) {
            // clip card effect for forground view
            // draw part1
            int save1 = canvas.save();
            if (mPercent > 0) {
                canvas.clipRect(0, 0, width, height / 2);
            } else {
                canvas.clipRect(0, height / 2, width, height);
            }
            super.draw(canvas);
            canvas.restoreToCount(save1);

            // draw part2
            if (mPercent < 0) {
                canvas.clipRect(0, 0, width, height / 2);
            } else {
                canvas.clipRect(0, height / 2, width, height);
            }
            mCamera.save();
            mCamera.setLocation(0f, 0f, -80);
            mCamera.rotateX(mPercent * 180);
            mCamera.getMatrix(mMatrix);
            mCamera.restore();
            mMatrix.preTranslate(-width / 2, -height / 2);
            mMatrix.postTranslate(width / 2, height / 2);
            canvas.concat(mMatrix);
            super.draw(canvas);

            mScrimPaint.setColor(0x08000000);
            canvas.drawRect(0, 0, width, height, mScrimPaint);
        } else {
            // draw shadow for underground view
            final int scrimColor = (int) (0xff * (1 - percent * 2)) << 24;
            mScrimPaint.setColor(scrimColor);

            if (mPercent < 0) {
                canvas.clipRect(0, 0, width, height / 2);
            } else {
                canvas.clipRect(0, height / 2, width, height);
            }

            super.draw(canvas);
            canvas.drawRect(0, 0, width, height, mScrimPaint);
        }
    }

}
