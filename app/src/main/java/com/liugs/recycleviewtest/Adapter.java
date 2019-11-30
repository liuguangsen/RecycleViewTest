package com.liugs.recycleviewtest;

import android.view.View;
import android.view.ViewGroup;

/**
 *
 */
public interface Adapter {
    /**
     * 创建ViewHolder
     *
     * @param position    item位置
     * @param convertView item布局
     * @param parent      RecyclerView
     * @return 返回item布局
     */
    View onCreateViewHolder(int position, View convertView, ViewGroup parent);

    /**
     * 绑定ViewHolder
     *
     * @param position    item位置
     * @param convertView item布局
     * @param parent      RecyclerView
     * @return 返回item布局
     */
    View onBinderViewHolder(int position, View convertView, ViewGroup parent);

    /**
     * 获取到当前row item的控件类型
     *
     * @param row 行标
     * @return 返回item的viewType
     */
    int getItemViewType(int row);

    /**
     * 获取当前控件类型的总数量
     */
    int getViewTypeCount();

    /**
     * 获取当前item的总数量
     */
    int getCount();

    /**
     * 获取index item的高度
     *
     * @param position
     * @return 对应position的高度
     */
    int getHeight(int position);
}
