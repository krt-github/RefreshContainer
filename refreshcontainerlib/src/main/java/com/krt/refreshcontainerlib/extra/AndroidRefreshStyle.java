package com.krt.refreshcontainerlib.extra;

import android.view.View;

import com.krt.refreshcontainerlib.RefreshContainer;

/**
 * Created by zehong.tang on 2017/4/13.
 * SwipeRefreshLayout style
 */

public class AndroidRefreshStyle extends OrdinaryRefreshStyle {

    public int[] getViewOrder() {
        return new int[]{VIEW_TYPE_CONTENT_VIEW, VIEW_TYPE_REFRESH_VIEW, VIEW_TYPE_LOAD_MORE_VIEW};
    }

    public void layoutView(View refreshView, View loadMoreView, View contentView, int left, int top, int right, int bottom) {
        super.layoutView(refreshView, loadMoreView, contentView, left, top, right, bottom);
        setMaxDistance(bottom / 3);
    }

    protected void handleScroll(int state, View refreshableView, View contentView, float offset, boolean isPullDown) {
        mMoveDistance += (int)offset;

        if(RefreshContainer.STATE_COMPLETE != state){
            refreshableView.setY(refreshableView.getTop() - calculateDestinationY(isPullDown));
        }
    }

    public void resetRefreshStyle(View refreshView, View loadMoreView, View contentView) {
        super.resetRefreshStyle(refreshView, loadMoreView, contentView);
        refreshView.setY(refreshView.getTop());
        loadMoreView.setY(loadMoreView.getTop());
    }
}
