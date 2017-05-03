package com.krt.refreshcontainerlib;

/**
 * Created by zehong.tang on 2017/4/7.
 * 刷新和加载更多的回调
 */

public interface IRefreshLoadMoreCallback {
    /**
     * 刷新时回调
     */
    void onRefresh();

    /**
     * 加载更多时回调
     */
    void onLoadMore();
}
