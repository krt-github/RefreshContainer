package com.krt.refreshcontainerdemo.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.krt.refreshcontainerlib.IRefreshView;
import com.krt.refreshcontainerlib.RefreshContainer;

/**
 * Created by zehong.tang on 2017/4/21.
 * Just copy https://github.com/tuesda/CircleRefreshLayout
 */

public class CircleRefreshView extends FrameLayout implements IRefreshView {
    private AnimationView mHeader;
    private ValueAnimator mUpTopAnimator;
    private float mHeaderHeight;
    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(10);

    public CircleRefreshView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CircleRefreshView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        mHeader = new AnimationView(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        mHeader.setLayoutParams(params);
        mHeader.setAniBackColor(0xFF33BBFF);
        mHeader.setAniForeColor(0xFFFFFFFF);
        mHeader.setRadius(6);

        setUpChildAnimation();
        addView(mHeader);
    }

    private void setUpChildAnimation() {
        mHeaderHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, AnimationView.ANIMATION_VIEW_HEIGHT, getResources().getDisplayMetrics());
        mUpTopAnimator = ValueAnimator.ofFloat(mHeaderHeight, 0);
        mUpTopAnimator.setDuration(500);
        mUpTopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                val = decelerateInterpolator.getInterpolation(val / mHeaderHeight) * val;
                mHeader.getLayoutParams().height = (int) val;
                mHeader.requestLayout();
            }
        });

        mHeader.setOnViewAniDone(new AnimationView.OnViewAniDone() {
            public void viewAniDone() {
                mUpTopAnimator.start();
            }
        });
    }

    public void onPull(int state, float pullDistance, float threshold) {
        switch(state){
            case RefreshContainer.STATE_PREPARE:
            case RefreshContainer.STATE_PREPARE_CANCEL:
            case RefreshContainer.STATE_READY:
            case RefreshContainer.STATE_PREPARE_WORK:
                mHeader.getLayoutParams().height = Math.abs((int) pullDistance);
                mHeader.requestLayout();
                break;
            case RefreshContainer.STATE_WORK:
                mHeader.releaseDrag();
                break;
            case RefreshContainer.STATE_COMPLETE:
                mHeader.setRefreshing(false);
                break;
            case RefreshContainer.STATE_IDLE:
                break;
            default:
                break;
        }
    }

    public int getDurationForCompletedAnimation() {
        return 1000;
    }
}
