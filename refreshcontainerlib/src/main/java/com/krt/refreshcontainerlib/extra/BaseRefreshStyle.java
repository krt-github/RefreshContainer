package com.krt.refreshcontainerlib.extra;

import android.view.View;

import com.krt.refreshcontainerlib.IRefreshStyle;

/**
 * Created by zehong.tang on 2017/4/13.
 * BaseRefreshStyle<br>
 * 默认提供 refreshView、loadMoreView、contentView 三者同步滑动行为
 */

public abstract class BaseRefreshStyle implements IRefreshStyle {
    private int mRefreshThreshold = 0;
    private int mLoadMoreThreshold = 0;
    private int mMaxDistance = Integer.MAX_VALUE;
    private float mDamping = 0.5f;
    /**
     * 0: original position, no move<br/>
     * negative: refresh view "show"<br/>
     * positive: load more view "show"
     */
    protected int mMoveDistance;

    public void onRefreshStateMove(int state, View refreshView, View contentView, float offset) {
        handleScroll(state, refreshView, contentView, offset * getDamping(), true);
    }

    public void onLoadMoreStateMove(int state, View loadMoreView, View contentView, float offset) {
        handleScroll(state, loadMoreView, contentView, offset * getDamping(), false);
    }

    public void onRefreshStateSettle(int state, View refreshView, View contentView, float offset) {
        handleScroll(state, refreshView, contentView, offset, true);
    }

    public void onLoadMoreStateSettle(int state, View loadMoreView, View contentView, float offset) {
        handleScroll(state, loadMoreView, contentView, offset, false);
    }

    protected void handleScroll(int state, View refreshableView, View contentView, float offset, boolean isPullDown){
        mMoveDistance += (int)offset;

        int destinationY = calculateDestinationY(isPullDown);
        ((View) refreshableView.getParent()).scrollTo(0, destinationY);
    }

    protected int calculateDestinationY(boolean isPullDown){
        int destination = mMoveDistance;

        //Process start edge
        if((isPullDown && mMoveDistance > 0)
                || (!isPullDown && mMoveDistance < 0)) {
            mMoveDistance = 0;
            destination = 0;
        }

        //Process end edge
        if(isReachMaxDistance(isPullDown)){
            destination = getMaxDistance(isPullDown);
        }

        return destination;
    }

    public boolean isReachRefreshThreshold() {
        return -(mMoveDistance) >= mRefreshThreshold;
    }

    public boolean isReachLoadMoreThreshold() {
        return mMoveDistance >= mLoadMoreThreshold;
    }

    public int getMoveDistance() {
        return mMoveDistance;
    }

    public int getRefreshThreshold() {
        return mRefreshThreshold;
    }

    public void setRefreshThreshold(int threshold){
        if(0 == mRefreshThreshold) { // Never assign value
            mRefreshThreshold = threshold;
        }
    }

    public int getLoadMoreThreshold() {
        return mLoadMoreThreshold;
    }

    public void setLoadMoreThreshold(int threshold){
        if(0 == mLoadMoreThreshold) { // Never assign value
            mLoadMoreThreshold = threshold;
        }
    }

    public void resetRefreshStyle(View refreshView, View loadMoreView, View contentView) {
        mMoveDistance = 0;
    }

    public float getDamping(){
        return mDamping;
    }

    public void setDamping(float damping){
        mDamping = damping;
    }

    public boolean isReachMaxDistance(boolean isPullDown){
        return Math.abs(mMoveDistance) >= Math.abs(mMaxDistance);
    }

    public void setMaxDistance(int maxDistance){
        if(Integer.MAX_VALUE == mMaxDistance) { // Never assign value
            mMaxDistance = maxDistance;
        }
    }

    public int getMaxDistance(boolean isPullDown){
        return mMaxDistance * (isPullDown ? -1 : 1);
    }

}
