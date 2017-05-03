package com.krt.refreshcontainerlib.extra.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.krt.refreshcontainerlib.IRefreshView;
import com.krt.refreshcontainerlib.R;
import com.krt.refreshcontainerlib.RefreshContainer;

/**
 * Created by zehong.tang on 2017/4/12.
 * BaseRefreshView
 */

public class BaseRefreshView extends FrameLayout implements IRefreshView {
    private TextView mTextView;
    private ImageView mImageView;

    private boolean mShowTextTip = true;
    private CharSequence mStringPullToWork = "下拉执行";
    private CharSequence mStringReleaseToWork = "松开执行";
    private CharSequence mStringWorking = "执行中...";
    private CharSequence mStringWorkCompleted = "执行完成";
    private int mBlinkDuration = 380;

    public BaseRefreshView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BaseRefreshView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        View root = LayoutInflater.from(context).inflate(R.layout.ordinary_refresh_view_layout, this, true);
        mTextView = (TextView) root.findViewById(R.id.text_view);
        mImageView = (ImageView) root.findViewById(R.id.image_view);
    }

    public BaseRefreshView setIcon(@DrawableRes int resId){
        mImageView.setImageResource(resId);
        return this;
    }

    void setPullToWorkString(CharSequence string){
        mStringPullToWork = string;
    }

    void setReleaseToWorkString(CharSequence string){
        mStringReleaseToWork = string;
    }

    void setWorkingString(CharSequence string){
        mStringWorking = string;
    }

    void setWorkCompletedString(CharSequence string){
        mStringWorkCompleted = string;
    }

    public BaseRefreshView setBlinkDuration(int duration){
        mBlinkDuration = duration;
        return this;
    }

    public BaseRefreshView setShowTipText(boolean show){
        mShowTextTip = show;
        mTextView.setVisibility(show ? View.VISIBLE : View.GONE);
        return this;
    }

    public BaseRefreshView setTextColor(@ColorInt int color){
        mTextView.setTextColor(color);
        return this;
    }

    public void onPull(int state, float pullDistance, float threshold) {
        switch(state){
            case RefreshContainer.STATE_PREPARE:
            case RefreshContainer.STATE_PREPARE_CANCEL:
                showTextTip(mStringPullToWork);
                break;
            case RefreshContainer.STATE_READY:
                showTextTip(mStringReleaseToWork);
                break;
            case RefreshContainer.STATE_PREPARE_WORK:
            case RefreshContainer.STATE_WORK:
                showTextTip(mStringWorking);
                blinkIcon();
                break;
            case RefreshContainer.STATE_COMPLETE:
                showTextTip(mStringWorkCompleted);
                stopBlinkIcon();
                break;
            default:
                break;
        }
    }

    public int getDurationForCompletedAnimation() {
        return 0;
    }

    private void showTextTip(CharSequence string){
        if(mShowTextTip){
            mTextView.setText(string);
        }
    }

    private ValueAnimator alphaAnimator;
    private void blinkIcon(){
        if(null == alphaAnimator) {
            alphaAnimator = ObjectAnimator.ofInt(mImageView, "imageAlpha", 255, 0);
            alphaAnimator.setInterpolator(new LinearInterpolator());
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            alphaAnimator.setDuration(mBlinkDuration);
        }
        if(!alphaAnimator.isRunning()) {
            alphaAnimator.start();
        }
    }

    private void stopBlinkIcon(){
        if(null != alphaAnimator && alphaAnimator.isRunning()){
            alphaAnimator.cancel();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mImageView.setImageAlpha(255);
        }else{
            mImageView.setAlpha(255);
        }
    }

}