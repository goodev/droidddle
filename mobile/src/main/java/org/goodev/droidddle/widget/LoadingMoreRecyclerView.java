package org.goodev.droidddle.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import org.goodev.droidddle.utils.Utils;

/**
 * Created by goodev on 2014/12/30.
 */
public class LoadingMoreRecyclerView extends BaseRecyclerView {
    private boolean mIsLoadingMore;
    private boolean mHasMoreData = true;
    private OnLoadingMoreListener mMoreListener;
    private int mFirstTop;

    public LoadingMoreRecyclerView(Context context) {
        super(context);
    }

    public LoadingMoreRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingMoreRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void checkLoadingMore(LoadingMoreRecyclerView recyclerView) {
        boolean firstVisible = false;

        LayoutManager manager = recyclerView.getLayoutManager();

        int visibleItemCount = manager.getChildCount();
        int totalItemCount = manager.getItemCount();
        int firstVisibleItems = 0;
        if (manager instanceof LinearLayoutManager) {
            firstVisibleItems = ((LinearLayoutManager) manager).findFirstVisibleItemPosition();
        } else if (manager instanceof GridLayoutManager) {
            firstVisibleItems = ((GridLayoutManager) manager).findFirstVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager sg = ((StaggeredGridLayoutManager) manager);
            int[] items = new int[sg.getSpanCount()];
            items = ((StaggeredGridLayoutManager) manager).findFirstVisibleItemPositions(items);
            firstVisibleItems = items[0];
        }

        boolean topOfFirstItemVisible = manager.getChildAt(0).getTop() >= recyclerView.mFirstTop;
        // enabling or disabling the refresh layout
        firstVisible = firstVisibleItems == 0 && topOfFirstItemVisible;

        mMoreListener.isFirstItemFullVisible(firstVisible);

        if (!mIsLoadingMore && mHasMoreData) {
            if ((visibleItemCount + firstVisibleItems) >= totalItemCount - 1) {
                if (!Utils.hasInternet(getContext())) {
                    return;
                }
                mIsLoadingMore = true;
                mMoreListener.onLoadingMore();
            }
        }
    }

    public void finishLoadingMore(boolean hasMoreData) {
        mIsLoadingMore = false;
        mHasMoreData = hasMoreData;
    }

    public void setOnLoadingMoreListener(OnLoadingMoreListener listener) {
        mMoreListener = listener;
    }

}
