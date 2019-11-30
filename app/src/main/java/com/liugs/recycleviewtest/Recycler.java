package com.liugs.recycleviewtest;

import android.view.View;

import java.util.Stack;

/**
 * 实现Item的缓存复用,
 */
public class Recycler {
    private Stack<View>[] views;

    public Recycler(int viewTypeCount) {
        views = new Stack[viewTypeCount];
        for (int i = 0; i < viewTypeCount; i++) {
            views[i] = new Stack<>();
        }
    }

    public void push(View view, int viewType) {
        views[viewType].push(view);
    }

    public View pop(int viewType) {
        try {
            return views[viewType].pop();
        } catch (Exception e) {
            // do nothing
        }
        return null;
    }
}
