package com.krt.refreshcontainerlib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.krt.refreshcontainerlib.extra.OrdinaryRefreshStyle;
import com.krt.refreshcontainerlib.extra.view.OrdinaryLoadMoreView;
import com.krt.refreshcontainerlib.extra.view.OrdinaryRefreshView;

/**
 * Created by zehong.tang on 2017/4/6.<br>
 * 为任意 View 提供垂直方向上的刷新和加载更多能力的容器，仅支持一个直接子 View(Content view) ，<br>
 * 刷新风格和刷新时展示的 View 均可自定义。<br>
 * {@link IRefreshLoadMoreCallback} 通知外部刷新或加载<br>
 * {@link IRefreshStyle} 定制刷新风格<br>
 * {@link IRefreshView} 获得本控件的刷新状态信息来更新自定义的刷新和加载 view <br>
 * 不要使用 addView 的方法添加 view，使用 {@link #setRefreshView(View)}、{@link #setLoadMoreView(View)}、<br>
 * {@link #setContentView(View)} 设置的 view 才会被布局。ContentView 亦可在 xml 文件中以直接子元素的形式设置<br>
 * 注意一定要调用 {@link #execute()} 使所有配置生效。<br><br>
 *
 * The RefreshContainer can customize your refresh style and refresh view, see
 * {@link IRefreshStyle}, {@link IRefreshView} . <br><br>
 *
 * The RefreshContainer should be used whenever the user can refresh and/or load the
 * contents of a view via a vertical swipe gesture. This container should be made
 * the parent of the view that will be refreshed as a result of the gesture and
 * can only support one direct child.The activity that instantiates this view should
 * add an IRefreshLoadMoreCallback to be notified whenever the swipe to refresh or
 * load gesture is completed.
 *
 * NOTICE: You must call {@link #execute()} bring into effect.
 */
public class RefreshContainer extends ViewGroup implements NestedScrollingParent{
    /**
     * 空闲状态
     */
    public static final int STATE_IDLE = 0;

    /**
     * 拉动控件，慢慢显露刷新或加载更多View
     */
    public static final int STATE_PREPARE = 1;

    /**
     * 拉动距离未达到门槛值松手，取消 work
     */
    public static final int STATE_PREPARE_CANCEL = 2;

    /**
     * 拉动距离达到门槛值未松手
     */
    public static final int STATE_READY = 3;

    /**
     * 拉动距离达到门槛值松手，进入 work 前的控件自动归位状态
     */
    public static final int STATE_PREPARE_WORK = 4;

    /**
     * work 状态
     */
    public static final int STATE_WORK = 5;

    /**
     * work 完成
     */
    public static final int STATE_COMPLETE = 6;

    private int mWorkState = STATE_IDLE;

    /**
     * 顶部刷新 View ，需实现 IRefreshView 接口，用于滑动过程中更新 View
     */
    private View mRefreshView;

    /**
     * 底部加载更多 View ，需实现 IRefreshView 接口，用于滑动过程中更新 View
     */
    private View mLoadMoreView;

    /**
     * 内容 View，STATE_IDLE(非刷新加载状态)下展示的 View，仅支持一个 contentView，宽高建议 MATCH_PARENT
     */
    private View mContentView;

    /**
     * 刷新风格，实现 IRefreshStyle 接口即可自定义刷新风格
     */
    private IRefreshStyle mRefreshStyle;

    /**
     * 刷新或加载更多回调 listener
     */
    private IRefreshLoadMoreCallback mRefreshLoadMoreCallback;

    /**
     * 操作条件检查者，判断是否可以刷新或加载
     */
    private IRefreshChecker mRefreshChecker;
    private boolean mEnableRefresh = true;
    private boolean mEnableLoadMore = true;
    private NestedScrollingParentHelper mNestedParentHelper;

    private static final float TOUCH_SLOP = 16;
    private static final int DEFAULT_SCROLL_DURATION = 380;
    private ValueAnimator mScroller;
    private int mScrollDuration = DEFAULT_SCROLL_DURATION;

    /**
     * true：在一个完整周期中，用户释放手势后，将禁止任何触摸事件，包括 contentView<br>
     * false：用户释放手指后，仅在 settle 过程中禁止触摸事件
     */
    private boolean mDisableTouchWhenWork = false;

    /**
     * 刷新区域的颜色。刷新区域指 mRefreshView 及之上的区域(如 mRefreshView 高度 300px，<br>
     * 刷新时下拉滑动的距离超过 300px，将会看到底色，该底色可能与整个风格不符)<br>
     * 设置该属性可平滑过渡到刷新区域，将 mRefreshView 的颜色延伸到之上的区域<br>
     * -1 没有设置，将忽略
     */
    private int mRefreshAreaColor = -1;

    /**
     * 加载更多区域的颜色。加载更多区域指 mLoadMoreView 及之下的区域，可参考 {@link RefreshContainer#mRefreshAreaColor}<br>
     * -1 没有设置，将忽略
     */
    private int mLoadMoreAreaColor = -1;

    public RefreshContainer(Context context) {
        super(context);
        init(context, null);
    }

    public RefreshContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RefreshContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        //TOUCH_SLOP = ViewConfiguration.get(context).getScaledTouchSlop();

        //像素值为整形。若使用 float ，在 scroll 或其他滚动 View 的操作中将损失小数部分
        //最后出现和预期位置相差几个到十几个像素
        mScroller = ValueAnimator.ofInt(0, 0); //ValueAnimator.ofFloat(0, 0);
        mScroller.addListener(new ScrollStateListener());
        mScroller.addUpdateListener(new ScrollUpdateListener());
        mScroller.setInterpolator(new DecelerateInterpolator());
        mNestedParentHelper = new NestedScrollingParentHelper(this);
        initAttribute(context, attrs);
    }

    private void initAttribute(Context context, AttributeSet attrs){
        if(null == attrs)
            return;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RefreshContainer);
        mScrollDuration = typedArray.getInt(R.styleable.RefreshContainer_duration, DEFAULT_SCROLL_DURATION);
        mDisableTouchWhenWork = typedArray.getBoolean(R.styleable.RefreshContainer_disableTouchWhenWork, false);

        typedArray.recycle();
    }

    public int getWorkState(){
        return mWorkState;
    }

    public int getDuration(){
        return mScrollDuration;
    }

    public RefreshContainer setDuration(int duration){
        mScrollDuration = duration;
        return this;
    }

    /**
     * 参见 {@link RefreshContainer#mRefreshAreaColor}
     * @param color color
     * @return instance of RefreshContainer
     */
    public RefreshContainer setRefreshAreaColor(int color){
        mRefreshAreaColor = color;
        setWillNotDraw(false);
        return this;
    }

    /**
     * 参见 {@link RefreshContainer#mLoadMoreAreaColor}
     * @param color color
     * @return instance of RefreshContainer
     */
    public RefreshContainer setLoadMoreAreaColor(int color){
        mLoadMoreAreaColor = color;
        setWillNotDraw(false);
        return this;
    }

    public RefreshContainer setInterpolator(TimeInterpolator interpolator){
        mScroller.setInterpolator(interpolator);
        return this;
    }

    public RefreshContainer setDisableTouchWhenWork(boolean disable){
        mDisableTouchWhenWork = disable;
        return this;
    }

    public RefreshContainer setRefreshLoadMoreCallback(IRefreshLoadMoreCallback l){
        mRefreshLoadMoreCallback = l;
        return this;
    }

    public RefreshContainer setEnableRefresh(boolean enabled){
        mEnableRefresh = enabled;
        return this;
    }

    public RefreshContainer setEnableLoadMore(boolean enabled){
        mEnableLoadMore = enabled;
        return this;
    }

    /**
     * 若子 View 支持 NestedScrollingChild ，可以不设置本 checker，将默认使用 Nested 方式
     * @param l checker
     * @return this
     */
    public RefreshContainer setRefreshChecker(IRefreshChecker l){
        mRefreshChecker = l;
        return this;
    }

    public RefreshContainer setRefreshStyle(IRefreshStyle style){
        mRefreshStyle = style;
        return this;
    }

    public RefreshContainer setContentView(View contentView){
        mContentView = contentView;
        return this;
    }

    public RefreshContainer setRefreshView(View refreshView){
        mRefreshView = refreshView;
        return this;
    }

    public RefreshContainer setLoadMoreView(View loadMoreView){
        mLoadMoreView = loadMoreView;
        return this;
    }

    /**
     * 当配置好后必须调用该方法使配置生效
     */
    public void execute(){
        checkArguments();
        removeAllViews();

        int[] viewOrder = getViewOrder();
        for (int order : viewOrder) {
            switch (order) {
                case IRefreshStyle.VIEW_TYPE_REFRESH_VIEW: addViewWithLayoutParams(mRefreshView);
                    break;
                case IRefreshStyle.VIEW_TYPE_LOAD_MORE_VIEW: addViewWithLayoutParams(mLoadMoreView);
                    break;
                case IRefreshStyle.VIEW_TYPE_CONTENT_VIEW: addViewWithLayoutParams(mContentView);
                    break;
                default:
                    break;
            }
        }
    }

    private void checkArguments(){
        if(null == mRefreshStyle){
            mRefreshStyle = new OrdinaryRefreshStyle();
        }

        if(null == mRefreshView){
            mRefreshView = new OrdinaryRefreshView(getContext());
        }
        if(!(mRefreshView instanceof IRefreshView)){
            throw new IllegalArgumentException("---Refresh view must implements IRefreshView---");
        }

        if(null == mLoadMoreView){
            mLoadMoreView = new OrdinaryLoadMoreView(getContext());
        }
        if(!(mLoadMoreView instanceof IRefreshView)){
            throw new IllegalArgumentException("---Load more view must implements IRefreshView---");
        }
    }

    public void setRefreshing(){
        setRefreshing(true);
    }

    public void setRefreshingDelay(int ms){
        setRefreshingDelay(ms, true);
    }

    public void setRefreshingDelay(int ms, final boolean animation){
        postDelayed(new Runnable() {
            public void run() {
                setRefreshing(animation);
            }
        }, ms);
    }

    public void setRefreshing(boolean animation){
        if(!mEnableRefresh)
            return;

        isPullDown = true;
        setWorkState(STATE_PREPARE_WORK);
        if(animation) {
            smoothScrollYTo(0, -getRefreshThreshold());
        }else{
            onRefreshStateSettle(-getRefreshThreshold());
            onSettleFinish();
        }
    }

    public void setWorkCompleted(){
        setWorkState(STATE_COMPLETE);
        updateUI();
    }

    /**
     * 外部加载任务完成，使用该方法通知本控件更新状态。<br>
     * @param runnable 若外部控件的加载结果需要在本控件回到 {@link #STATE_IDLE} 后再呈现，可使用该参数
     *                 封装呈现逻辑。
     */
    public void setWorkCompleted(Runnable runnable){
        setWorkCompleted();
        mRunnableWhenCycleEnd = runnable;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutView(left, top, right, bottom);
        this.right = right;
        this.bottom = bottom;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        int refreshLoadMoreViewCount = (null == mRefreshView ? 0 : 1) + (null == mLoadMoreView ? 0 : 1);
        int contentViewCount = getChildCount() - refreshLoadMoreViewCount;
        if(contentViewCount > 1){
            throw new IllegalArgumentException("---RefreshContainer only can hold one content view---");
        }else if(1 == contentViewCount){
            mContentView = getChildAt(0);
        }
    }

    protected void onDraw(Canvas canvas) {
        drawDecorationColor(canvas);
        super.onDraw(canvas);
    }

    private int right;
    private int bottom;
    private final Paint decorationPaint = new Paint();
    private void drawDecorationColor(Canvas canvas) {
        if(-1 != mRefreshAreaColor && mRefreshView.getVisibility() == View.VISIBLE){
            decorationPaint.setColor(mRefreshAreaColor);
            canvas.drawRect(0, -bottom, right, 0, decorationPaint);
        }
        if(-1 != mLoadMoreAreaColor && mLoadMoreView.getVisibility() == View.VISIBLE){
            decorationPaint.setColor(mLoadMoreAreaColor);
            canvas.drawRect(0, bottom, right, 2 * bottom, decorationPaint);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int childCount = getChildCount();
        LayoutParams layoutParams;
        View childView;
        int width;
        int height;
        for(int i = 0; i < childCount; i++){
            childView = getChildAt(i);
            layoutParams = childView.getLayoutParams();
            width = makeMeasureSpec(layoutParams.width, widthMeasureSpec);
            height = makeMeasureSpec(layoutParams.height, heightMeasureSpec);
            childView.measure(width, height);
        }
    }

    private int makeMeasureSpec(int childSize, int parentSize){
        int result;
        if(childSize >= 0) {
            result = MeasureSpec.makeMeasureSpec(childSize, MeasureSpec.EXACTLY);
        }else if(LayoutParams.MATCH_PARENT == childSize){
            result = MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.EXACTLY);
        }else if(LayoutParams.WRAP_CONTENT == childSize){
            result = MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.AT_MOST);
        }else{
            result = MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.UNSPECIFIED);
        }
        return result;
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isNestedScrollMode() && 0 != (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        final int distance = getMoveDistance();
        if((distance < 0 && dy > 0) //Refresh view "show" && finger move up
                || (distance > 0 && dy < 0)){ //Load more view "show" && finger move down
            handleMove(dy);
            consumed[1] = dy;
        }
        super.onNestedPreScroll(target, dx, dy, consumed);
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        final int distance = getMoveDistance();
        if(0 == distance){ // Neither refresh view nor load more view not shown
            if(null == isPullDown && 0 != dyUnconsumed) {
                if (dyUnconsumed < 0){
                    isPullDown = mEnableRefresh ? true : null;
                }else{
                    isPullDown = mEnableLoadMore ? false : null;
                }
            }
        }
        handleMove(dyUnconsumed);
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return 0 != getMoveDistance();
    }

    public void onStopNestedScroll(View child) {
        mNestedParentHelper.onStopNestedScroll(child);
        handleUp();
    }

    public int getNestedScrollAxes() {
        return mNestedParentHelper.getNestedScrollAxes();
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mScroller.isRunning() || (mDisableTouchWhenWork && STATE_WORK == getWorkState())) {
            switch(ev.getAction()){
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    interceptTouchEvent = false;
                    break;
                default:
                    break;
            }
            return false;
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean interceptTouchEvent = false;
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(isNestedScrollMode())
            return false;
        if(interceptTouchEvent)
            return true;

        if(STATE_WORK != getWorkState() && isReadyForWork()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    lastY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(shouldIntercept(event)) {
                        event.setAction(MotionEvent.ACTION_DOWN);
                        interceptTouchEvent = true;
                        return true;
                    }
                    break;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    private boolean shouldIntercept(MotionEvent event){
        float offsetY = lastY - event.getY();
        if(isValidTouch(startX - event.getX(), offsetY)){
            if(offsetY < 0){
                if(isReadyForRefresh()) {
                    isPullDown = true;
                    return true;
                }
            }else{
                if(isReadyForLoadMore()){
                    isPullDown = false;
                    return true;
                }
            }
        }
        return false;
    }

    private void attemptDetectDirection(MotionEvent event){
        shouldIntercept(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(isReadyForWork()){
            handleTouchEvent(event);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private float startX;
    private float lastY;
    /**
     * null : STATE_IDLE 或 方向错误<br>
     * true : 下拉(refresh)<br>
     * false : 上拉(load more)
     */
    private Boolean isPullDown = null;
    private void handleTouchEvent(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                startX = 0;
                lastY = 0;
                handleUp();
                interceptTouchEvent = false;
                break;
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //isPullDown 需要多次 onInterceptTouchEvent 才能判断出来，如果子 View 没有消费 touchEvent，
                //将极有可能没有判断出方向，这种情况需要在这里继续(多次)判断
                if(null == isPullDown){
                    attemptDetectDirection(event);
                }else{
                    handleMove(lastY - event.getY());
                }
                lastY = event.getY();
                break;
            default:
                break;
        }
    }

    private void handleMove(float offsetY){
        if(null == isPullDown) {
            return;
        }

        if(STATE_WORK != getWorkState()) {
            boolean isReachThreshold = isPullDown ? isReachRefreshThreshold() : isReachLoadMoreThreshold();
            setWorkState(isReachThreshold ? STATE_READY : STATE_PREPARE);
        }

        if(isPullDown){
            onRefreshStateMove(offsetY);
        }else{
            onLoadMoreStateMove(offsetY);
        }
    }

    private boolean isValidTouch(float offsetX, float offsetY){
        if(null != isPullDown)
            return true;

        final float x = Math.abs(offsetX);
        final float y = Math.abs(offsetY);
        if((x < TOUCH_SLOP && y < TOUCH_SLOP) || (x > y))
            return false;

        return true;
    }

    private void handleUp(){
        switch(getWorkState()){
            default:
                return;
            case STATE_PREPARE: setWorkState(STATE_PREPARE_CANCEL);
                break;
            case STATE_READY:
                if(null != mRefreshLoadMoreCallback){
                    setWorkState(STATE_PREPARE_WORK);
                }else{
                    setWorkState(STATE_COMPLETE);
                }
                break;
            case STATE_WORK: //处理在 work 状态时，再次下拉或上拉的情况
                break;
        }

        updateUI();
    }

    private void updateUI(){
        switch(getWorkState()){
            default:
                return;
            case STATE_PREPARE_CANCEL: smoothScrollYTo(getMoveDistance(), 0);
                break;
            case STATE_WORK: //处理在 work 状态时，再次下拉或上拉的情况
            case STATE_PREPARE_WORK:
                if(null == isPullDown) {
                    return;
                }
                smoothScrollYTo(getMoveDistance(), isPullDown ? -getRefreshThreshold() : getLoadMoreThreshold());
                break;
            case STATE_COMPLETE:
                notifyRefreshableView();
                smoothScrollYTo(getMoveDistance(), 0, getStartDelay());
                break;
        }
    }

    private void smoothScrollYTo(float startY, float targetY){
        smoothScrollYTo(startY, targetY, 0);
    }

    private void smoothScrollYTo(float startY, float targetY, int delayMs){
        settleLastValue = (int)startY;

        if(mScroller.isRunning()){
            isCanceled = true;
            mScroller.cancel();
        }
        mScroller.setIntValues((int)startY, (int)targetY); //mScroller.setFloatValues(startY, targetY);
        mScroller.setDuration(getDuration());
        mScroller.setStartDelay(delayMs);
        mScroller.start();
    }

    private int getStartDelay(){
        if(null == isPullDown) {
            return 0;
        }

        IRefreshView view = (IRefreshView)(isPullDown ? mRefreshView : mLoadMoreView);
        return view.getDurationForCompletedAnimation();
    }

    private boolean isReadyForRefresh(){
        return mEnableRefresh && (isNestedScrollMode() || mRefreshChecker.checkCanDoRefresh(this, mContentView));
    }

    private boolean isReadyForLoadMore(){
        return mEnableLoadMore && (isNestedScrollMode() || mRefreshChecker.checkCanDoLoadMore(this, mContentView));
    }

    private boolean isReadyForWork(){
        return isReadyForRefresh() || isReadyForLoadMore();
    }

    /**
     * isNestedScrollMode
     * @return true: 使用 nested 滑动<br/>
     * false：使用 mRefreshChecker 配合 onTouchEvent 滑动
     */
    private boolean isNestedScrollMode(){
        return null == mRefreshChecker;
    }

    private void setWorkState(int state){
        mWorkState = state;
    }

    //---------------------------以下方法是对 mRefreshStyle 封装---------------------------

    private void layoutView(int left, int top, int right, int bottom){
        if(null != mRefreshStyle) {
            mRefreshStyle.layoutView(mRefreshView, mLoadMoreView, mContentView, left, top, right, bottom);
        }
    }

    private void onRefreshStateMove(float offset){
        mRefreshView.setVisibility(View.VISIBLE);
        mRefreshStyle.onRefreshStateMove(getWorkState(), mRefreshView, mContentView, (int)offset);
        notifyRefreshableView();
    }

    private void onLoadMoreStateMove(float offset){
        mLoadMoreView.setVisibility(View.VISIBLE);
        mRefreshStyle.onLoadMoreStateMove(getWorkState(), mLoadMoreView, mContentView, (int)offset);
        notifyRefreshableView();
    }

    private void notifyRefreshableView(){
        if(null == isPullDown) {
            return;
        }

        IRefreshView refreshView;
        float threshold;
        if(isPullDown){
            refreshView = (IRefreshView) mRefreshView;
            threshold = getRefreshThreshold();
        }else{
            refreshView = (IRefreshView) mLoadMoreView;
            threshold = getLoadMoreThreshold();
        }

        refreshView.onPull(getWorkState(), isReachMaxDistance() ? getMaxDistance() : getMoveDistance(), threshold);
    }

    private void onRefreshStateSettle(float offset) {
        mRefreshStyle.onRefreshStateSettle(getWorkState(), mRefreshView, mContentView, offset);
    }

    private void onLoadMoreStateSettle(float offset) {
        mRefreshStyle.onLoadMoreStateSettle(getWorkState(), mLoadMoreView, mContentView, offset);
    }

    private int[] getViewOrder(){
        return mRefreshStyle.getViewOrder();
    }

    private boolean isReachRefreshThreshold(){
        return mRefreshStyle.isReachRefreshThreshold();
    }

    private boolean isReachLoadMoreThreshold(){
        return mRefreshStyle.isReachLoadMoreThreshold();
    }

    private int getMoveDistance(){
        return mRefreshStyle.getMoveDistance();
    }

    private float getRefreshThreshold() {
        return mRefreshStyle.getRefreshThreshold();
    }

    private float getLoadMoreThreshold() {
        return mRefreshStyle.getLoadMoreThreshold();
    }

    private void resetRefreshStyle(){
        mRefreshStyle.resetRefreshStyle(mRefreshView, mLoadMoreView, mContentView);
    }

    private int getMaxDistance(){
        return mRefreshStyle.getMaxDistance(isPullDown);
    }

    private boolean isReachMaxDistance(){
        return mRefreshStyle.isReachMaxDistance(isPullDown);
    }

    //---------------------------以上方法是对 mRefreshStyle 封装---------------------------

    private void addViewWithLayoutParams(View view){
        if(null == view.getLayoutParams()){
            view.setLayoutParams(getDefaultLayoutParams());
        }
        addView(view);
    }

    private LayoutParams getDefaultLayoutParams(){
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    private void updateRefreshableViewVisibility(){
        final int distance = getMoveDistance();
        mRefreshView.setVisibility(distance < 0 ? View.VISIBLE : View.INVISIBLE);
        mLoadMoreView.setVisibility(distance > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    private Runnable mRunnableWhenCycleEnd;
    private void onCycleEnded(){
        if(null != mRunnableWhenCycleEnd){
            mRunnableWhenCycleEnd.run();
        }
        mRunnableWhenCycleEnd = null;
    }

    /**
     * 强制 settle 。避免在 setWorkState(STATE_WORK) 后某些异常情况导致不能通过 onRefresh 、onLoadMore<br>
     * 的方式结束 work ，将使用 setWorkCompleted() 强制完成 work。在 smoothScrollYTo 中若 mScroller.isRunning()<br>
     * 则会 isCanceled = true ，导致 scroll 完成后无法进入 IDLE ，使用 forceSettle 处理这种情况。<br><br>
     *
     * 针对 (null == mRefreshLoadMoreCallback || null == isPullDown) 的情况<br>
     * 已做过处理，真实环境中应该不会出现上述情况。
     */
    private boolean forceSettle = false;
    private void onSettleFinish(){
        if(!forceSettle && isCanceled){
            isCanceled = false;
            return;
        }

        isCanceled = false;
        forceSettle = false;

        switch(getWorkState()){
            default:
                return;
            case STATE_PREPARE_WORK:
                setWorkState(STATE_WORK);
                if(null == mRefreshLoadMoreCallback || null == isPullDown) {
                    setWorkCompleted();
                    forceSettle = true;
                    break;
                }
                if(isPullDown)
                    mRefreshLoadMoreCallback.onRefresh();
                else
                    mRefreshLoadMoreCallback.onLoadMore();
                break;
            case STATE_COMPLETE: setWorkState(STATE_IDLE);
                break;
            case STATE_PREPARE_CANCEL: setWorkState(STATE_IDLE);
                break;
        }

        notifyRefreshableView();
        if(STATE_IDLE == getWorkState()){
            resetRefreshStyle();
            updateRefreshableViewVisibility();
            isPullDown = (0 == getMoveDistance() ? null : isPullDown);
            onCycleEnded();
        }
    }

    private void onSettle(float offset){
        if(null == isPullDown) {
            return;
        }

        if(isPullDown){
            onRefreshStateSettle(offset);
        }else{
            onLoadMoreStateSettle(offset);
        }
    }

    private int settleLastValue = 0;
    private class ScrollUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        public void onAnimationUpdate(ValueAnimator animation) {
            int currentValue = (int)animation.getAnimatedValue();
            onSettle(currentValue - settleLastValue);
            notifyRefreshableView();
            settleLastValue = currentValue;
        }
    }

    private boolean isCanceled = false;
    private class ScrollStateListener extends AnimatorListenerAdapter{
        public void onAnimationEnd(Animator animation) {
            onSettleFinish();
        }
    }

}
