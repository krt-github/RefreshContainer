package com.krt.refreshcontainerlib;

import android.view.View;

/**
 * Created by zehong.tang on 2017/4/7.<br>
 * 自定义刷新风格接口，主要是 刷新view、加载view 和内容view 三者间的布局和拉动时 view 间的互动关系。<br>
 * Interface for refresh and load style.
 */

public interface IRefreshStyle {
    /**
     * View 类型，刷新 view <br>
     * 通过 {@link #getViewOrder} 返回 3种 view 间的层级关系
     */
    int VIEW_TYPE_REFRESH_VIEW = 0;

    /**
     * View 类型，加载更多 view <br>
     * 通过 {@link #getViewOrder} 返回 3种 view 间的层级关系
     */
    int VIEW_TYPE_LOAD_MORE_VIEW = 1;

    /**
     * View 类型，内容 view <br>
     * 通过 {@link #getViewOrder} 返回 3种 view 间的层级关系
     */
    int VIEW_TYPE_CONTENT_VIEW = 2;

    /**
     * 获取 3种 view 间的层级关系
     * @return length 为3，元素值为 {@link IRefreshStyle#VIEW_TYPE_REFRESH_VIEW}、
     * {@link IRefreshStyle#VIEW_TYPE_LOAD_MORE_VIEW}、{@link IRefreshStyle#VIEW_TYPE_CONTENT_VIEW} 的无重复数组
     */
    int[] getViewOrder();

    /**
     * 由 {@link com.krt.refreshcontainerlib.RefreshContainer#layout} 调用，自定义 3种 view 的初始位置
     * @param refreshView 刷新 view
     * @param loadMoreView 加载更多 view
     * @param contentView 内容 view
     * @param left RefreshContainer 提供的布局范围 left
     * @param top RefreshContainer 提供的布局范围 top
     * @param right RefreshContainer 提供的布局范围 right
     * @param bottom RefreshContainer 提供的布局范围 bottom
     */
    void layoutView(View refreshView, View loadMoreView, View contentView, int left, int top, int right, int bottom);

    /**
     * 自定义刷新状态下 refreshView 和 contentView 如何变化
     * @param state RefreshContainer 当前刷新状态
     * @param refreshView refreshView
     * @param contentView contentView
     * @param offset 滑动变化量
     */
    void onRefreshStateMove(int state, View refreshView, View contentView, float offset);

    /**
     * 自定义加载更多状态下 loadMoreView 和 contentView 如何变化
     * @param state RefreshContainer 当前刷新状态
     * @param loadMoreView loadMoreView
     * @param contentView contentView
     * @param offset 滑动变化量
     */
    void onLoadMoreStateMove(int state, View loadMoreView, View contentView, float offset);

    /**
     * 刷新状态下用户释放手指，View 如何自动回到刷新或初始位置
     * @param state RefreshContainer 当前刷新状态
     * @param refreshView refreshView
     * @param contentView contentView
     * @param offset 滑动变化量
     */
    void onRefreshStateSettle(int state, View refreshView, View contentView, float offset);

    /**
     * 加载更多状态下用户释放手指，View 如何自动回到加载或初始位置
     * @param state RefreshContainer 当前刷新状态
     * @param loadMoreView loadMoreView
     * @param contentView contentView
     * @param offset 滑动变化量
     */
    void onLoadMoreStateSettle(int state, View loadMoreView, View contentView, float offset);

    /**
     * 判断滑动距离是否达到刷新门槛值。
     * 该条件一般情况可使用 {@link IRefreshStyle#getRefreshThreshold()} 判断<br>
     * 该方法可自定义更细致的风格
     * @return true：滑动距离大于门槛值
     */
    boolean isReachRefreshThreshold();

    /**
     * 判断滑动距离是否达到加载更多门槛值
     * 该条件一般情况可使用 {@link IRefreshStyle#getLoadMoreThreshold()} 判断<br>
     * 该方法可自定义更细致的风格
     * @return true：滑动距离大于门槛值
     */
    boolean isReachLoadMoreThreshold();

    /**
     * 获取本次操作产生的滑动距离，该距离并不一定等于 view 滑动的距离，如 view 的滑动有阻尼系数<br>
     * 注意滑动距离大于 {@link #getMaxDistance(boolean)} 时 view 会停留在该位置，但 moveDistance 仍会继续“增长”。
     * @return 本次操作产生的滑动距离
     */
    int getMoveDistance();

    /**
     * 获取刷新门槛值，用于自动回到刷新位置 {@link IRefreshStyle#onRefreshStateSettle}
     * @return 刷新门槛值
     */
    int getRefreshThreshold();

    /**
     * 获取加载更多门槛值，用于自动回到加载位置 {@link IRefreshStyle#onLoadMoreStateSettle}
     * @return 加载门槛值
     */
    int getLoadMoreThreshold();

    /**
     * 复位操作，完成刷新或加载后回调，注意复位 {@link IRefreshStyle#getMoveDistance()} 中记录的距离
     * @param refreshView refreshView
     * @param loadMoreView loadMoreView
     * @param contentView contentView
     */
    void resetRefreshStyle(View refreshView, View loadMoreView, View contentView);

    /**
     * 阻尼系数
     * @return damping
     */
    float getDamping();

    /**
     * 获取 View 最大移动距离，当 View 移动了 maxDistance 后将不再随手指移动更多距离
     * @param isPullDown 指定要获取哪个方向上的最大距离
     * @return max distance
     */
    int getMaxDistance(boolean isPullDown);

    /**
     * View 滑动距离是否大于等于 maxDistance。
     * @param isPullDown 指定要判断的方向
     * @return true: 真实距离大于等于 maxDistance
     */
    boolean isReachMaxDistance(boolean isPullDown);

}
