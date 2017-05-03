package com.krt.refreshcontainerdemo;

import android.view.View;

import com.krt.refreshcontainerdemo.view.FlyRefreshView;
import com.krt.refreshcontainerlib.RefreshContainer;
import com.krt.refreshcontainerlib.extra.ParallaxRefreshStyle;

/**
 * Created by zehong.tang on 2017/4/19.
 * FlyRefreshStyle
 */

public class FlyRefreshStyle extends ParallaxRefreshStyle {

    public FlyRefreshStyle(){
        setParallaxMultiplier(0);
        setZoomWhenReachThreshold(false);
    }

    public void layoutView(View refreshView, View loadMoreView, View contentView, int left, int top, int right, int bottom) {
        super.layoutView(refreshView, loadMoreView, contentView, left, top, right, bottom);
        setMaxDistance(refreshView.getMeasuredHeight() + 150);
    }

    public void onRefreshStateMove(int state, View refreshView, View contentView, float offset) {
        offset *= getDamping();
        handleScroll(state, refreshView, contentView, offset, true);
        offsetParkView(refreshView, offset);
    }

    private boolean recordReachState = false;
    private void offsetParkView(View flyRefreshView, float offset){
        if(flyRefreshView instanceof FlyRefreshView){
            if(isReachRefreshThreshold()){
                recordReachState = true;
                ((FlyRefreshView) flyRefreshView).resetParkView();
            }else{
                if(recordReachState){
                    offset = getRefreshThreshold() + mMoveDistance;
                }
                recordReachState = false;
                ((FlyRefreshView) flyRefreshView).updateParkViewY(-(int)offset);
            }
        }
    }

    public void onRefreshStateSettle(int state, View refreshView, View contentView, float offset) {
        if(RefreshContainer.STATE_PREPARE_WORK == state || RefreshContainer.STATE_WORK == state) {
            super.onRefreshStateSettle(state, refreshView, contentView, offset);
            offsetParkView(refreshView, offset);
        }
    }

    public void resetRefreshStyle(View refreshView, View loadMoreView, View contentView) {
    }

}
