package com.project.scan_on.Helper;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeAnimation extends Animation {
    private View mView;

    private float mToWidth;
    private float mFromWidth;

    public ResizeAnimation(View v, float toWidth, float fromWidth) {
        mToWidth = toWidth;
        mFromWidth = fromWidth;
        mView = v;
        setDuration(3000);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
        ViewGroup.LayoutParams p = mView.getLayoutParams();
        p.width = (int) width;
        mView.requestLayout();
    }
}