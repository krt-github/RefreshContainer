package com.krt.refreshcontainerdemo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.krt.refreshcontainerdemo.R;
import com.krt.refreshcontainerlib.IRefreshView;
import com.krt.refreshcontainerlib.RefreshContainer;

/**
 * Created by zehong.tang on 2017/4/20.
 * FlyRefreshView
 * MountainSceneView just copy from https://github.com/race604/FlyRefresh
 */

public class FlyRefreshView extends FrameLayout implements IRefreshView {
    private static final int PARK_VIEW_SIZE = 56; //dp
    private static final int FLY_DURATION = 800;

    private MountainSceneView mMountainSceneView;
    private ImageView mFlyView;
    private View mParkView;

    public FlyRefreshView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FlyRefreshView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        final float density = getResources().getDisplayMetrics().density;
        final int bottomMargin = (int) (5 * density);
        final int parkSize = (int)(PARK_VIEW_SIZE * density);

        mMountainSceneView = new MountainSceneView(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.bottomMargin = parkSize / 2 + bottomMargin;
        addView(mMountainSceneView, layoutParams);

        View bottomBGView = new View(context);
        bottomBGView.setBackgroundColor(0xFFFFFFFF);
        layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, parkSize / 2 + bottomMargin);
        layoutParams.gravity = Gravity.BOTTOM;
        addView(bottomBGView, layoutParams);

        mParkView = new View(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mParkView.setZ(10);
        }
        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.getPaint().setColor(0xFF61B3DD);
        mParkView.setBackgroundDrawable(circle);
        layoutParams = new LayoutParams(parkSize, parkSize);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        layoutParams.leftMargin = (int)(17 * density);
        layoutParams.bottomMargin = bottomMargin;
        addView(mParkView, layoutParams);

        final int flySize = (int)(32 * density);
        mFlyView = new ImageView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mFlyView.setZ(10);
        }
        setFlyResource(R.mipmap.icon_send);
        layoutParams = new LayoutParams(flySize, flySize);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        layoutParams.leftMargin = (int)(29 * density);
        layoutParams.bottomMargin = (int)(12 * density) + bottomMargin;
        addView(mFlyView, layoutParams);
    }

    public void setFlyResource(int resId){
        mFlyView.setImageResource(resId);
    }

    public void updateParkViewY(int offset){
        mParkView.setY(mParkView.getY() + offset);
        mFlyView.setY(mFlyView.getY() + offset);
    }

    public void resetParkView(){
        mParkView.setY(mParkView.getTop());
        mFlyView.setY(mFlyView.getTop());
    }

    public void onPull(int state, float pullDistance, float threshold) {
        switch(state){
            case RefreshContainer.STATE_PREPARE:
            case RefreshContainer.STATE_PREPARE_CANCEL:
                break;
            case RefreshContainer.STATE_READY:
                float ratio = (Math.abs(pullDistance / threshold) - 1) * 2;
                mMountainSceneView.onPullProgress(state, ratio);
                rotationFlyView(ratio);
                break;
            case RefreshContainer.STATE_PREPARE_WORK:
                mMountainSceneView.onPullProgress(state, (Math.abs(pullDistance / threshold) - 1) * 2);
                startFly();
                break;
            case RefreshContainer.STATE_WORK:
                break;
            case RefreshContainer.STATE_COMPLETE:
                flyBack();
                break;
            case RefreshContainer.STATE_IDLE:
                isFlyBackRunning = false;
                break;
            default:
                break;
        }
    }

    private AnimatorSet mFlyAnimatorSet;
    private AnimatorSet mFlyBackAnimatorSet;
    private boolean isFlyBackRunning = false;
    private void startFly() {
        if(null == mFlyAnimatorSet){
            ObjectAnimator rotation = ObjectAnimator.ofFloat(mFlyView, "rotation", -45, 0);
            ObjectAnimator rotationX = ObjectAnimator.ofFloat(mFlyView, "rotationX", 0, 60);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFlyView, "scaleX", 1, 0.5f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFlyView, "scaleY", 1, 0.5f);
            ObjectAnimator transX = ObjectAnimator.ofFloat(mFlyView, "translationX", 0, getWidth());
            ObjectAnimator transY = ObjectAnimator.ofFloat(mFlyView, "translationY", 0, -getHeight());
            transY.setInterpolator(PathInterpolatorCompat.create(0.7f, 1f));

            mFlyAnimatorSet = new AnimatorSet();
            mFlyAnimatorSet.setDuration(FLY_DURATION);
            mFlyAnimatorSet.playTogether(transX, transY, rotationX, scaleX, scaleY, rotation);
        }
        if(!mFlyAnimatorSet.isRunning()){
            clearAnimator(mFlyView);
            mFlyAnimatorSet.start();
        }
    }

    private void flyBack() {
        if(null == mFlyBackAnimatorSet){
            final int offDistX = -mFlyView.getRight();
            final int offDistY = -30;
            AnimatorSet flyDownAnim = new AnimatorSet();
            flyDownAnim.setDuration(FLY_DURATION);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(mFlyView, "rotation", mFlyView.getRotation(), 0);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFlyView, "scaleX", 0.3f, 0.8f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFlyView, "scaleY", 0.3f, 0.8f);
            ObjectAnimator transX = ObjectAnimator.ofFloat(mFlyView, "translationX", getWidth(), offDistX);
            ValueAnimator transY = ValueAnimator.ofFloat(-getHeight(), offDistY);
            transY.setInterpolator(PathInterpolatorCompat.create(0.1f, 1f));
            transY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float y = (float) animation.getAnimatedValue();
                    mFlyView.setTranslationY(y + mParkView.getTranslationY());
                }
            });

            flyDownAnim.playTogether(transX, transY, scaleX, scaleY, rotation);
            flyDownAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    mFlyView.setRotationY(180);
                }
                public void onAnimationEnd(Animator animation) {
                    mFlyView.setRotationY(0);
                }
            });

            AnimatorSet flyInAnim = new AnimatorSet();
            flyInAnim.setDuration(FLY_DURATION / 2);
            flyInAnim.setInterpolator(new DecelerateInterpolator());
            scaleX = ObjectAnimator.ofFloat(mFlyView, "scaleX", 0.8f, 1f);
            scaleY = ObjectAnimator.ofFloat(mFlyView, "scaleY", 0.8f, 1f);
            transX = ObjectAnimator.ofFloat(mFlyView, "translationX", offDistX, 0);
            transY = ValueAnimator.ofFloat(offDistY, 0);
            transY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float y = (float) animation.getAnimatedValue();
                    mFlyView.setTranslationY(y + mParkView.getTranslationY());
                }
            });
            ObjectAnimator rotationX = ObjectAnimator.ofFloat(mFlyView, "rotationX", 30, 0);
            flyInAnim.playTogether(transX, transY, rotationX, scaleX, scaleY);
            flyInAnim.setStartDelay(100);
            flyInAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    mFlyView.setRotationY(0);
                }
            });

            mFlyBackAnimatorSet = new AnimatorSet();
            mFlyBackAnimatorSet.playSequentially(flyDownAnim, flyInAnim);
        }
        if(!isFlyBackRunning){
            clearAnimator(mFlyView);
            mFlyBackAnimatorSet.start();
            isFlyBackRunning = true;
        }
    }

    private void rotationFlyView(float progress) {
        mFlyView.setRotation(Math.min(0, Math.max(-45, (-45) * progress)));
    }

    private void clearAnimator(View v) {
        ViewCompat.setAlpha(v, 1);
        ViewCompat.setScaleY(v, 1);
        ViewCompat.setScaleX(v, 1);
        ViewCompat.setTranslationY(v, 0);
        ViewCompat.setTranslationX(v, 0);
        ViewCompat.setRotation(v, 0);
        ViewCompat.setRotationY(v, 0);
        ViewCompat.setRotationX(v, 0);
        // to do https://code.google.com/p/android/issues/detail?id=80863
        // ViewCompat.setPivotY(v, v.getMeasuredHeight() / 2);
        v.setPivotY(v.getMeasuredHeight() / 2);
        ViewCompat.setPivotX(v, v.getMeasuredWidth() / 2);
        ViewCompat.animate(v).setInterpolator(null);
    }

    public int getDurationForCompletedAnimation() {
        return 800;
    }
}
