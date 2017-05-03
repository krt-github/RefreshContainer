package com.krt.refreshcontainerlib.extra.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.krt.refreshcontainerlib.IRefreshView;
import com.krt.refreshcontainerlib.RefreshContainer;

/**
 * Created by zehong.tang on 2017/4/13.
 * AndroidStyleRefreshView 模仿 SwipeRefreshLayout 风格
 */

public class AndroidStyleRefreshView extends FrameLayout implements IRefreshView {
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;

    private CircleImageView mCircleView;
    private MaterialProgressDrawable mProgress;
    private int[] mColorScheme = new int[]{0xFF6699FF, 0xFF99CC33, 0xFFFF6600};

    public AndroidStyleRefreshView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AndroidStyleRefreshView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        mProgress = new MaterialProgressDrawable(context, this);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mProgress.setColorSchemeColors(mColorScheme);
        mCircleView = new CircleImageView(context, CIRCLE_BG_LIGHT);
        mCircleView.setImageDrawable(mProgress);

        final float density = getResources().getDisplayMetrics().density;
        final int size = (int)(density * 40);
        final int margin = (int)(density * 10);
        LayoutParams layoutParams = new LayoutParams(size, size);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(0, margin, 0, margin);
        addView(mCircleView, layoutParams);

        slingshotDist = 64/*DEFAULT_CIRCLE_TARGET*/ * density;
    }

    public void setColorSchemeColors(int... colors){
        mProgress.setColorSchemeColors(colors);
    }

    public void onPull(int state, float pullDistance, float threshold) {
        switch(state){
            case RefreshContainer.STATE_PREPARE:
            case RefreshContainer.STATE_PREPARE_CANCEL:
            case RefreshContainer.STATE_READY:
                moveSpinner(pullDistance, threshold);
                break;
            case RefreshContainer.STATE_PREPARE_WORK:
            case RefreshContainer.STATE_WORK:
                refreshingSpinner();
                break;
            case RefreshContainer.STATE_COMPLETE:
                scaleDownSpinner(Math.abs(pullDistance) / threshold);
                break;
            case RefreshContainer.STATE_IDLE:
                resetSpinner();
                break;
            default:
                break;
        }
    }

    private void refreshingSpinner(){
        if (!mProgress.isRunning()) {
            mProgress.showArrow(false);
            mProgress.setAlpha(255);
            mProgress.start();
        }
    }

    private void resetSpinner(){
        mProgress.stop();
        mCircleView.setScaleX(1f);
        mCircleView.setScaleY(1f);
    }

    private void scaleDownSpinner(float scaleRatio){
        mCircleView.setScaleX(scaleRatio);
        mCircleView.setScaleY(scaleRatio);
    }

    private float slingshotDist;
    private void moveSpinner(float overScrollTop, float totalDragDistance) {
        float originalDragPercent = overScrollTop / totalDragDistance;
        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
        float extraOS = Math.abs(overScrollTop) - totalDragDistance;
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
        float tensionPercent = (float)((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
        float strokeStart = adjustedPercent * .8f;
        float rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f;

        mProgress.setStartEndTrim(0f, Math.min(/*MAX_PROGRESS_ANGLE*/0.8f, strokeStart));
        mProgress.setProgressRotation(rotation);
        mProgress.setArrowScale(Math.min(1f, adjustedPercent));
        mProgress.setAlpha((int)(255 * Math.abs(originalDragPercent)));
        mProgress.showArrow(true);
    }

    public int getDurationForCompletedAnimation() {
        return 0;
    }

}
