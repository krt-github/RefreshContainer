package com.krt.refreshcontainerlib.extra.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.krt.refreshcontainerlib.IRefreshView;
import com.krt.refreshcontainerlib.RefreshContainer;

/**
 * Created by zehong.tang on 2017/4/14.
 * ParallaxRefreshView
 */

public class ParallaxRefreshView extends FrameLayout implements IRefreshView {
    private ImageView mBlinkImageView;
    private ImageView mMaskImageView;
    private ImageView mCompleteImageView;

    public ParallaxRefreshView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ParallaxRefreshView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        setBackgroundColor(0xFF000000);
        mBlinkImageView = new ImageView(context);
        mMaskImageView = new ImageView(context);
        mCompleteImageView = new ImageView(context);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mBlinkImageView.setLayoutParams(layoutParams);
        mBlinkImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMaskImageView.setLayoutParams(layoutParams);
        mMaskImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mCompleteImageView.setLayoutParams(layoutParams);
        mCompleteImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mCompleteImageView.setVisibility(View.INVISIBLE);

        addView(mBlinkImageView);
        addView(mMaskImageView);
        addView(mCompleteImageView);
    }

    public ImageView getBlinkImageView(){
        return mBlinkImageView;
    }

    public ImageView getMaskImageView(){
        return mMaskImageView;
    }

    public ImageView getCompleteImageView(){
        return mCompleteImageView;
    }

    public void onPull(int state, float pullDistance, float threshold) {
        switch(state){
            case RefreshContainer.STATE_PREPARE:
            case RefreshContainer.STATE_PREPARE_CANCEL:
                break;
            case RefreshContainer.STATE_READY:
                break;
            case RefreshContainer.STATE_PREPARE_WORK:
            case RefreshContainer.STATE_WORK:
                blinkIcon();
                break;
            case RefreshContainer.STATE_COMPLETE:
                stopBlinkIcon();
                break;
            case RefreshContainer.STATE_IDLE:
                reset();
                break;
            default:
                break;
        }
    }

    private ValueAnimator alphaAnimator;
    private void blinkIcon(){
        if(null == alphaAnimator) {
            alphaAnimator = ObjectAnimator.ofInt(mBlinkImageView, "imageAlpha", 255, 0);
            alphaAnimator.setInterpolator(new LinearInterpolator());
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            alphaAnimator.setDuration(500);
        }
        if(!alphaAnimator.isRunning()) {
            alphaAnimator.start();
        }
    }

    private void stopBlinkIcon(){
        if(null != alphaAnimator && alphaAnimator.isRunning()){
            alphaAnimator.cancel();

            if(null != mCompleteImageView.getDrawable()) {
                mCompleteImageView.setVisibility(View.VISIBLE);
                mCompleteImageView.setScaleX(5);
                mCompleteImageView.setScaleY(5);
                mCompleteImageView.animate().scaleX(1).scaleY(1).setDuration(380).start();
            }
        }
    }

    private void reset(){
        mCompleteImageView.setVisibility(INVISIBLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBlinkImageView.setImageAlpha(255);
        }else{
            mBlinkImageView.setAlpha(255);
        }
    }

    public int getDurationForCompletedAnimation() {
        return null == mCompleteImageView.getDrawable() ? 0 : 1200;
    }
}
