package com.krt.refreshcontainerlib.extra;

import android.view.View;

/**
 * Created by zehong.tang on 2017/4/7.
 * ParallaxRefreshStyle
 */

public class ParallaxRefreshStyle extends BaseRefreshStyle{
    /**
     * 产生视差滞后的效果，滞后的 View 移动距离为正常值 * mParallaxMultiplier
     * 有效值范围 [0, 1] => [完全不动，完全跟随]
     */
    private float mParallaxMultiplier = 0.5f;
    private boolean mZoomWhenReachThreshold = true;

    public ParallaxRefreshStyle(){
        setDamping(0.8f);
    }

    public int[] getViewOrder() {
        return new int[]{VIEW_TYPE_REFRESH_VIEW, VIEW_TYPE_LOAD_MORE_VIEW, VIEW_TYPE_CONTENT_VIEW};
    }

    public void layoutView(View refreshView, View loadMoreView, View contentView, int left, int top, int right, int bottom) {
        int refreshViewHeight = refreshView.getMeasuredHeight();
        int loadMoreViewHeight = loadMoreView.getMeasuredHeight();
        setRefreshThreshold(refreshViewHeight);
        setLoadMoreThreshold(loadMoreViewHeight);

        final float pivotX = (right - left) / 2;
        float pivotY = refreshViewHeight * getParallaxMultiplier();
        int offset = (int) pivotY;
        refreshView.layout(left, -offset, right, refreshViewHeight - offset);
        refreshView.setPivotX(pivotX);
        refreshView.setPivotY(pivotY);

        pivotY = loadMoreViewHeight * (1 - getParallaxMultiplier());
        offset = (int) pivotY;
        loadMoreView.layout(left, bottom - offset, right, bottom + (loadMoreViewHeight - offset));
        loadMoreView.setPivotX(pivotX);
        loadMoreView.setPivotY(pivotY);

        if(null != contentView){
            contentView.layout(left, top, right, bottom);
        }
    }

    protected void handleScroll(int state, View refreshableView, View contentView, float offset, boolean isPullDown) {
        mMoveDistance += (int)offset;

        int destinationY = calculateDestinationY(isPullDown);
        contentView.setY(-destinationY);
        parallax(refreshableView, isPullDown);
    }

    protected void parallax(View view, boolean isPullDown){
        final int moveDistance = isReachMaxDistance(isPullDown) ? getMaxDistance(isPullDown) : mMoveDistance;
        view.setY(view.getTop() - (moveDistance * getParallaxMultiplier()));
        if(mZoomWhenReachThreshold && (isReachRefreshThreshold() || isReachLoadMoreThreshold())){
            float scale = (float)Math.abs(moveDistance) / view.getHeight();
            view.setScaleX(scale);
            view.setScaleY(scale);
        }
    }

    /**
     * 产生视差滞后的效果，滞后的 View 移动距离为正常值 * mParallaxMultiplier
     * 有效值范围 [0, 1] => [完全不动，完全跟随]
     */
    public void setParallaxMultiplier(float value){
        if(value < 0 || value > 1)
            value = 0.5f;
        mParallaxMultiplier = value;
    }

    public float getParallaxMultiplier() {
        return mParallaxMultiplier;
    }

    public void setZoomWhenReachThreshold(boolean zoom){
        mZoomWhenReachThreshold = zoom;
    }

    public boolean isEnableZoomWhenReachThreshold(){
        return mZoomWhenReachThreshold;
    }
}
