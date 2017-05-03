package com.krt.refreshcontainerlib.extra.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by zehong.tang on 2017/4/11.
 * OrdinaryRefreshView
 */

public class OrdinaryRefreshView extends BaseRefreshView {

    public OrdinaryRefreshView(@NonNull Context context) {
        super(context);
        initString();
    }

    public OrdinaryRefreshView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initString();
    }

    private void initString() {
        setPullToWorkString("下拉刷新");
        setReleaseToWorkString("松开刷新");
        setWorkingString("刷新中...");
        setWorkCompletedString("刷新完成");
    }

    public OrdinaryRefreshView setPullToRefreshString(CharSequence string){
        setPullToWorkString(string);
        return this;
    }

    public OrdinaryRefreshView setReleaseToRefreshString(CharSequence string){
        setReleaseToWorkString(string);
        return this;
    }

    public OrdinaryRefreshView setRefreshingString(CharSequence string){
        setWorkingString(string);
        return this;
    }

    public OrdinaryRefreshView setRefreshCompletedString(CharSequence string){
        setWorkCompletedString(string);
        return this;
    }

}
