package com.krt.refreshcontainerlib.extra.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by zehong.tang on 2017/4/11.
 * OrdinaryLoadMoreView
 */

public class OrdinaryLoadMoreView extends BaseRefreshView{

    public OrdinaryLoadMoreView(@NonNull Context context) {
        super(context);
        initString();
    }

    public OrdinaryLoadMoreView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initString();
    }

    private void initString() {
        setPullToWorkString("上拉加载");
        setReleaseToWorkString("松开加载");
        setWorkingString("加载中...");
        setWorkCompletedString("加载完成");
    }

    public OrdinaryLoadMoreView setPullToLoadMoreString(CharSequence string){
        setPullToWorkString(string);
        return this;
    }

    public OrdinaryLoadMoreView setReleaseToLoadMoreString(CharSequence string){
        setReleaseToWorkString(string);
        return this;
    }

    public OrdinaryLoadMoreView setLoadingString(CharSequence string){
        setWorkingString(string);
        return this;
    }

    public OrdinaryLoadMoreView setLoadMoreCompletedString(CharSequence string){
        setWorkCompletedString(string);
        return this;
    }
}
