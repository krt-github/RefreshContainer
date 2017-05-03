package com.krt.refreshcontainerdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.krt.refreshcontainerdemo.view.AnimationView;
import com.krt.refreshcontainerdemo.view.CircleRefreshView;
import com.krt.refreshcontainerdemo.view.FlyRefreshView;
import com.krt.refreshcontainerlib.IRefreshLoadMoreCallback;
import com.krt.refreshcontainerlib.RefreshContainer;
import com.krt.refreshcontainerlib.extra.AndroidRefreshStyle;
import com.krt.refreshcontainerlib.extra.OrdinaryRefreshStyle;
import com.krt.refreshcontainerlib.extra.ParallaxRefreshStyle;
import com.krt.refreshcontainerlib.extra.view.AndroidStyleRefreshView;
import com.krt.refreshcontainerlib.extra.view.OrdinaryLoadMoreView;
import com.krt.refreshcontainerlib.extra.view.OrdinaryRefreshView;
import com.krt.refreshcontainerlib.extra.view.ParallaxRefreshView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zehong.tang on 2017/5/2.
 * Demo
 */

public class DemoActivity extends AppCompatActivity {
    private RefreshContainer mRefreshContainer;
    private MyAdapter mAdapter;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_layout);
        init();
    }

    private void init() {
        initRefreshContainer();
        initOtherView();
        loadLastStyle(lastStyle);
        mRefreshContainer.setRefreshingDelay(500);
    }

    private void initRefreshContainer(){
        mRefreshContainer = (RefreshContainer) findViewById(R.id.refresh_container);
        mRefreshContainer.setDuration(320)
                .setInterpolator(new DecelerateInterpolator())
//                .setRefreshChecker(new IRefreshChecker()) //子 View 不支持嵌套滑动需要设置 checker
                .setRefreshLoadMoreCallback(new IRefreshLoadMoreCallback() {
                    public void onRefresh() {simulateTask(-1);}
                    public void onLoadMore() {simulateTask(mAdapter.getItemCount());}
                })
                .execute();
    }

    private void setOrdinaryStyle(){
        OrdinaryRefreshView refreshView = new OrdinaryRefreshView(getApplicationContext());
        refreshView.setPullToRefreshString("Pull to refresh")
                .setReleaseToRefreshString("Release to refresh")
                .setRefreshingString("Refreshing...")
                .setRefreshCompletedString("Refresh completed")
                .setIcon(R.mipmap.odn_refresh)
                .setShowTipText(true)
                .setTextColor(0xFF666666)
                .setBlinkDuration(380);

        OrdinaryLoadMoreView loadMoreView = new OrdinaryLoadMoreView(getApplicationContext());
        loadMoreView.setIcon(R.mipmap.odn_refresh)
                .setShowTipText(false)
                .setTextColor(0xFF666666)
                .setBlinkDuration(380);

        mRefreshContainer.setRefreshView(refreshView)
                .setLoadMoreView(loadMoreView)
                .setRefreshStyle(new OrdinaryRefreshStyle())
                .setRefreshAreaColor(-1)
                .setLoadMoreAreaColor(-1)
                .setEnableLoadMore(true)
                .execute();
    }

    private void setParallaxStyle(){
        ParallaxRefreshView refreshView = new ParallaxRefreshView(getApplicationContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(228));
        refreshView.setLayoutParams(layoutParams);
        refreshView.getBlinkImageView().setImageResource(R.mipmap.plx_refresh);

        ParallaxRefreshView loadMoreView = new ParallaxRefreshView(getApplicationContext());
        layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(142));
        loadMoreView.setLayoutParams(layoutParams);
        loadMoreView.getBlinkImageView().setImageResource(R.mipmap.plx_load_more);
        loadMoreView.getMaskImageView().setImageResource(R.mipmap.plx_load_more_mask);
        loadMoreView.getCompleteImageView().setImageResource(R.mipmap.plx_complete);

        mRefreshContainer.setRefreshStyle(new ParallaxRefreshStyle())
                .setRefreshView(refreshView)
                .setLoadMoreView(loadMoreView)
                .setEnableLoadMore(true)
                .execute();
    }

    private void setAndroidStyle(){
        AndroidStyleRefreshView refreshView = new AndroidStyleRefreshView(getApplicationContext());
        AndroidStyleRefreshView loadMoreView = new AndroidStyleRefreshView(getApplicationContext());
        AndroidRefreshStyle style = new AndroidRefreshStyle();
        style.setMaxDistance(dp2px(170));

        mRefreshContainer.setRefreshView(refreshView)
                .setLoadMoreView(loadMoreView)
                .setRefreshStyle(style)
                .setRefreshAreaColor(-1)
                .setLoadMoreAreaColor(-1)
                .setEnableLoadMore(true)
                .execute();
    }

    private void setCircleStyle(){
        final int viewHeight = dp2px(AnimationView.ANIMATION_VIEW_HEIGHT);
        final int pullDelta = dp2px(AnimationView.ANIMATION_VIEW_PULL_DELTA);
        CircleRefreshView refreshView = new CircleRefreshView(getApplicationContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, viewHeight + pullDelta);
        refreshView.setLayoutParams(layoutParams);

        ParallaxRefreshStyle circleRefreshStyle = new ParallaxRefreshStyle();
        circleRefreshStyle.setRefreshThreshold(viewHeight);
        circleRefreshStyle.setMaxDistance(viewHeight + pullDelta);
        circleRefreshStyle.setParallaxMultiplier(0);
        circleRefreshStyle.setZoomWhenReachThreshold(false);

        mRefreshContainer.setRefreshStyle(circleRefreshStyle)
                .setDuration(380)
                .setRefreshView(refreshView)
                .setRefreshAreaColor(-1)
                .setLoadMoreAreaColor(-1)
                .setEnableLoadMore(false)
                .execute();
    }

    private void setFlyStyle(){
        FlyRefreshView flyRefreshView = new FlyRefreshView(getApplicationContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(250));
        flyRefreshView.setLayoutParams(layoutParams);

        FlyRefreshStyle flyRefreshStyle = new FlyRefreshStyle();
        flyRefreshStyle.setDamping(0.8f);
        flyRefreshStyle.setZoomWhenReachThreshold(false);

        mRefreshContainer.setRefreshStyle(flyRefreshStyle)
                .setDuration(600)
                .setRefreshView(flyRefreshView)
                .setRefreshAreaColor(-1)
                .setLoadMoreAreaColor(-1)
                .setEnableLoadMore(false)
                .execute();
    }

    private void initOtherView(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new RecyclerViewDivider());
        mAdapter = new MyAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    private int dp2px(int dp){
        return (int)(getResources().getDisplayMetrics().scaledDensity * dp);
    }

    private static int lastStyle = 0;
    private boolean loadLastStyle(int style){
        switch(style){
            case 0: lastStyle = 0; setOrdinaryStyle();
                return true;
            case 1: lastStyle = 1; setParallaxStyle();
                return true;
            case 2: lastStyle = 2; setAndroidStyle();
                return true;
            case 3: lastStyle = 3; setCircleStyle();
                return true;
            case 4: lastStyle = 4; setFlyStyle();
                return true;
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "OrdinaryStyle");
        menu.add(0, 1, 1, "ParallaxStyle");
        menu.add(0, 2, 2, "AndroidStyle");
        menu.add(0, 3, 3, "CircleStyle");
        menu.add(0, 4, 4, "FlyStyle");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return loadLastStyle(item.getItemId()) || super.onOptionsItemSelected(item);
    }

    private Random random = new Random();
    private int[] drawableList = {R.mipmap.ic_assessment_white_24dp, R.mipmap.ic_folder_white_24dp,
            R.mipmap.ic_info_grey600_18dp, R.mipmap.ic_smartphone_white_24dp, R.mipmap.ic_search_white_24dp};
    private List<ItemBean> generateData(int index){
        int start = index < 0 ? 0 : index;
        List<ItemBean> list = new ArrayList<>();
        ItemBean itemBean;
        String style = "";
        switch(lastStyle){
            case 0: style = "Ordinary style"; break;
            case 1: style = "Parallax style"; break;
            case 2: style = "Android style"; break;
            case 3: style = "Circle style"; break;
            case 4: style = "Fly style"; break;
        }
        for(int i = start; i < 30 + start; i++){
            itemBean = new ItemBean();
            itemBean.text = style + " index: " + i;
            itemBean.color = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            itemBean.icon = drawableList[random.nextInt(5)];
            itemBean.checked = itemBean.color % 2 == 0;
            list.add(itemBean);
        }
        return list;
    }

    private void simulateTask(final int index){
        mRefreshContainer.postDelayed(new Runnable() {
            public void run() {
                if(index < 0)
                    mAdapter.clearData();
                mAdapter.addData(generateData(index));
                mAdapter.notifyDataSetChanged();
                mRefreshContainer.setWorkCompleted();
            }
        }, 3500);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
        private final List<ItemBean> mData = new ArrayList<>();
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_layout, parent, false);
            return new MyViewHolder(view);
        }
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.bindData(mData.get(position));
        }
        public int getItemCount() {
            return mData.size();
        }
        public void addData(List<ItemBean> data){
            mData.addAll(data);
        }
        public void clearData(){
            mData.clear();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView textView;
        private ImageView imageView;
        private CheckBox checkBox;
        private ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
            imageView = (ImageView) itemView.findViewById(R.id.icon);
            checkBox = (CheckBox) itemView.findViewById(R.id.check_box);
        }
        public void bindData(ItemBean data){
            textView.setText(data.text);
            shapeDrawable.getPaint().setColor(data.color);
            imageView.setBackgroundDrawable(shapeDrawable);
            imageView.setImageResource(data.icon);
            checkBox.setChecked(data.checked);
        }
    }

    private class ItemBean{
        String text;
        int color;
        int icon;
        boolean checked;
    }

    private class RecyclerViewDivider extends RecyclerView.ItemDecoration {
        private static final int SPAN = 1;
        private Drawable mDrawable = new ColorDrawable(0xFFCCCCCC);
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0, 0, 0, SPAN);
        }

        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int childCount = parent.getChildCount();
            int width = parent.getWidth();
            View child;
            for(int i = 0; i < childCount - 1; i++){
                child = parent.getChildAt(i);
                mDrawable.setBounds(0, child.getBottom(), width, child.getBottom() + SPAN);
                mDrawable.draw(c);
            }
        }
    }

}
