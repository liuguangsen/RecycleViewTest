package com.liugs.recycleviewtest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.util.ArrayList;

public class RecyclerView extends ViewGroup {

    private int touchTapSlop;
    private ArrayList<View> viewList;
    private boolean needRelayout;

    private int rowCount;
    private int[] heights;
    private int width;
    private int height;

    private Adapter adapter;
    // 上下偏移量
    //当前滑动的Y值
    private int currentY;
    private int scrollY;
    private int firstRow;
    private Recycler recycler;

    public RecyclerView(Context context) {
        this(context, null);
    }

    public RecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.touchTapSlop = ViewConfiguration.get(getContext()).getScaledDoubleTapSlop();
        viewList = new ArrayList<>();
        needRelayout = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (adapter != null) {
            rowCount = adapter.getCount();
            heights = new int[rowCount];
            for (int i = 0; i < heights.length; i++) {
                heights[i] = adapter.getHeight(i);
            }
        }
        int tempHeight = sumArray(heights, 0, heights.length);
        int h = Math.min(tempHeight, height);
        setMeasuredDimension(width, h);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed || needRelayout) {
            needRelayout = false;
            viewList.clear();
            removeAllViews();
            // 摆放适配器的view
            if (adapter != null) {
                width = r - l;
                height = b - t;
                int top = 0, right, bottom, left = 0;
                for (int i = 0; i < rowCount && top < height; i++) {
                    right = width;
                    bottom = top + heights[i];
                    View view = makeAndLayout(i, left, top, right, bottom);
                    viewList.add(view);
                    top = bottom;
                }
            }
        }
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        int key = (int) view.getTag(R.id.tag_type_view);
        recycler.push(view, key);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //初始化手指触摸点  用来记录初始点
                currentY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                //取出手指触摸的初始点 跟当前手指的位置得出两者相减的结果的正数值
                float y2 = Math.abs(currentY - ev.getRawY());
                //如果滑动的距离大于最小滑动距离  就拦截事件
                if (y2 > touchTapSlop) {
                    intercept = true;
                }
                break;
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //先获取到当前Y轴坐标
                float y2 = event.getRawY();
                //手指触摸点减去当前Y轴坐标 正为上滑 负为下滑
                float diffY = currentY - y2;
                //将滑动到的位置赋值给初始点
                currentY = (int) y2;    //不加会影响反应速度
                //进行滑动的方法
                scrollBy(0, (int) diffY);
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void scrollBy(int x, int y) {
        //累加Y坐标偏移值
        scrollY += y;
        scrollY = scrollBounds(scrollY);
        //如果scrollY大于0 上滑
        if (scrollY > 0) {  //上滑   上滑加载下面的
            //上滑移除上面的
            //如果当前滚动的Y轴大于当前行的高度  第一次滑动firstRow=0
            while (scrollY > heights[firstRow]) {
                //删除这一行  就是删除当前布局中的第0个ItemView
                removeView(viewList.remove(0));
                //改变scrollY scrollY要减去这一行的高度
                scrollY -= heights[firstRow];
                //当前显示的行标++
                firstRow++;
            }
            //上滑加载下面的
            //判断当前内容的高度是不是小于RecyclerView的高度  如果小于  就添加新Item
            while (getFillHeight() < height) {
                //首先  当前第一行的行标 加上以及显示的itemView的长度 其实就是要添加进去的那一行的Item
                int addlast = firstRow + viewList.size();
                //然后获取到这一个Item的View
                View view = obtainView(addlast, width, heights[addlast]);
                //将item的View添加到当前显示的最下面
                viewList.add(viewList.size(), view);
            }
        } else if (scrollY < 0) {    //如果scrollY小于0 下滑
            //下滑加载  判断是否小于0  如果小于0  就没必要再下拉了
            while (scrollY < 0) {
                //下滑和上滑刚好相反 当前显示的Item的第一行的行标减去1
                int firstAddRow = firstRow - 1;
                //获取到当前view显示的第一个ItemView的上一个itemView
                View view = obtainView(firstAddRow, width, heights[firstAddRow]);
                //添加到当前显示的itenView的最前面
                viewList.add(0, view);
                //当前显示的第一行的行标减1
                firstRow--;
                //改变scrollY scrollY要江山当前添加进去的行的行高
                scrollY += heights[firstRow];
            }
            //下滑的过程中要将最上面的itemView移除掉  判断当前显示的view的总高度是不是大于RecyvlerView的高度 如果大于 就将最上面的itemView移除掉
            while (sumArray(heights, firstRow, viewList.size()) - scrollY - heights[firstRow + viewList.size() - 1] >= height) {
                //移除掉向前显示的第一个Item
                removeView(viewList.remove(viewList.size() - 1));
            }
        } else {

        }
        //重新摆放控件的位置
        rePositionView();
        //super.scrollBy(x, y);

    }

    /**
     * 当控件滚动到极限值的时候  就不再进行滚动
     *
     * @param scrollY
     * @return
     */
    private int scrollBounds(int scrollY) {
        //如果滚钉的scrollY大于0
        if (scrollY > 0) {
            //判断上滑的极限值  防止滚动的距离  大于当前所有内容的高度
            scrollY = Math.min(scrollY, sumArray(heights, firstRow, heights.length - firstRow) - height);
        } else {
            //判断下滑的极限值  防止滚动的距离 小于第0个item的高度
            scrollY = Math.max(scrollY, -sumArray(heights, 0, firstRow));
        }
        return scrollY;
    }

    /**
     * 重新摆放位置
     */
    private void rePositionView() {
        //定义上下左右
        int top = 0, right, bottom, left = 0, i;
        //上边等于top-scrollY  因为scrollY是负数   -将是+ +就是-
        top = -scrollY;
        //将当前行的值赋值给I
        i = firstRow;
        for (View view : viewList) {
            //下移一个或者上移一个
            bottom = top + heights[i++];
            view.layout(0, top, width, bottom);
            top = bottom;
        }

    }

    /**
     * 获取当前屏幕中内容的总高度
     *
     * @return
     */
    private int getFillHeight() {
        return sumArray(heights, firstRow, viewList.size()) - scrollY;
    }

    /**
     * 获取数组中指定部分的高度
     *
     * @param heights 所有子view的高度的数组
     * @param index   开始子view的下标
     * @param count   获取子view的数目
     * @return 获取高度值
     */
    private int sumArray(int[] heights, int index, int count) {
        int sum = 0;
        count += index;
        for (int i = index; i < count; i++) {
            sum += heights[i];
        }
        return sum;
    }

    private View makeAndLayout(int row, int left, int top, int right, int bottom) {
        View view = obtainView(row, right - left, bottom - top);
        view.layout(left, top, right, bottom);
        return view;
    }

    private View obtainView(int row, int width, int height) {
        int itemViewType = adapter.getItemViewType(row);
        View view = recycler.pop(itemViewType);
        // 定义一个itemView
        View itemView = null;
        if (view == null){
            itemView = adapter.onCreateViewHolder(row,itemView,this);
            if (itemView == null){
                throw new RuntimeException("onCreateViewHolder return null");
            }
        } else {
            itemView = adapter.onBinderViewHolder(row,view,this);
        }
        itemView.setTag(R.id.tag_type_view,itemViewType);
        itemView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        addView(itemView);
        return itemView;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        if (adapter != null) {
            recycler = new Recycler(adapter.getViewTypeCount());
            scrollY = 0;
            firstRow = 0;
            needRelayout = true;
            // 重新摆放下
            requestLayout();
        }
    }
}
