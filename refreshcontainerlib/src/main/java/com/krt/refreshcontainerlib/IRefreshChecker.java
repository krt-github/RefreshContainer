package com.krt.refreshcontainerlib;

import android.view.View;

/**
 * Created by zehong.tang on 2017/4/10.
 * 刷新加载条件检查
 */

public interface IRefreshChecker {
    /**
     * 刷新条件检查
     * @param container RefreshContainer
     * @param contentView content view
     * @return true：可以刷新
     */
    boolean checkCanDoRefresh(RefreshContainer container, View contentView);

    /**
     * 加载更多条件检查
     * @param container RefreshContainer
     * @param contentView contentView
     * @return true：可以加载更多
     */
    boolean checkCanDoLoadMore(RefreshContainer container, View contentView);
}
