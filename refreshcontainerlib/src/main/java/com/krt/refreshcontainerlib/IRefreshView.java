package com.krt.refreshcontainerlib;

/**
 * Created by zehong.tang on 2017/4/10.
 * 用于获取 {@link com.krt.refreshcontainerlib.RefreshContainer} 的刷新状态，根据状态更新 view
 */

public interface IRefreshView {
    /**
     * 当 {@link com.krt.refreshcontainerlib.RefreshContainer} 离开 {@link com.krt.refreshcontainerlib.RefreshContainer#STATE_IDLE} <br>
     * 状态时回调，用来告知 refreshableView 当前的状态信息，根据此信息更新内部 view (如 文字、图片、透明度等)<br>
     * 注意同一种状态下多次调用的情况，避免重复执行某些逻辑
     * @param state {@link com.krt.refreshcontainerlib.RefreshContainer} 当前状态
     * @param pullDistance 滑动距离(View 真实移动距离，非手指滑动距离)
     * @param threshold 刷新或加载的门槛值，该值与 pullDistance 可计算出滑动比例，用于渐变的场景。
     */
    void onPull(int state, float pullDistance, float threshold);

    /**
     * 当完成任务由 {@link com.krt.refreshcontainerlib.RefreshContainer#STATE_WORK} 进入
     * {@link com.krt.refreshcontainerlib.RefreshContainer#STATE_COMPLETE} 状态前，可对视图
     * 设置一个完成动画，该动画时间将作为进入 {@link com.krt.refreshcontainerlib.RefreshContainer#STATE_COMPLETE}
     * 状态的延时时间
     * @return 播放 “完成动画” 所需的时间
     */
    int getDurationForCompletedAnimation();
}
