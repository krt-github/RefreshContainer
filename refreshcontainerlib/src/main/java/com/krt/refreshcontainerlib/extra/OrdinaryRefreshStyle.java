package com.krt.refreshcontainerlib.extra;

import android.view.View;

import com.krt.refreshcontainerlib.IRefreshStyle;

/**
 * Created by zehong.tang on 2017/4/7.
 * OrdinaryRefreshStyle
 */

public class OrdinaryRefreshStyle extends BaseRefreshStyle {

    @Override
    public int[] getViewOrder() {
        return new int[]{IRefreshStyle.VIEW_TYPE_REFRESH_VIEW, IRefreshStyle.VIEW_TYPE_CONTENT_VIEW,
                        IRefreshStyle.VIEW_TYPE_LOAD_MORE_VIEW};
    }

    @Override
    public void layoutView(View refreshView, View loadMoreView, View contentView, int left, int top, int right, int bottom) {
        int refreshViewHeight = refreshView.getMeasuredHeight();
        int loadMoreViewHeight = loadMoreView.getMeasuredHeight();
        setRefreshThreshold(refreshViewHeight);
        setLoadMoreThreshold(loadMoreViewHeight);

        refreshView.layout(left, -refreshViewHeight, right, 0);
        loadMoreView.layout(left, bottom, right, bottom + loadMoreViewHeight);
        if(null != contentView){
            contentView.layout(left, top, right, bottom);
        }
    }

}
