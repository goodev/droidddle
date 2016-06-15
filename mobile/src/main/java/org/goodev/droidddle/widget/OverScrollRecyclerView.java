package org.goodev.droidddle.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import org.goodev.droidddle.utils.Utils;

/**
 * Created by ADMIN on 2014/12/26.
 */
public class OverScrollRecyclerView extends RecyclerView {
    private static final float MIN_VELOCITY = 60f;
    private static final float MIN_BOTTOM_VY = -60f;
    final AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }
    };
    //EmptyView like listView ---------
    View mEmptyView;
    ProgressView mLoadingView;
    private VelocityTracker mTracker;
    private float mYVelocity;
    private OnOverScrollListener mOverListener;
    //loading more data when scroll to last
    private OnLoadingMoreListener mMoreListener;
    private boolean mIsLoadingMore;
    private boolean mHasMoreData = true;

    public OverScrollRecyclerView(Context context) {
        super(context);
    }

    public OverScrollRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverScrollRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                initTracker(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTracker == null) {
                    initTracker(ev);
                } else {
                    mTracker.addMovement(ev);
                }
                mTracker.computeCurrentVelocity(100);
                mYVelocity = mTracker.getYVelocity();
                boolean firstVisible = false;

                LayoutManager manager = getLayoutManager();

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

                if (firstVisibleItems == 0) {
                    View child = manager.getChildAt(0);
                    if (child != null) {
                        LayoutParams lp = (LayoutParams) child.getLayoutParams();
                        firstVisible = manager.getChildAt(0).getTop() >= (getPaddingTop() + lp.topMargin);
                    }
                }

                // enabling or disabling the refresh layout
                if (mOverListener != null && mYVelocity > MIN_VELOCITY && firstVisible) {
                    mOverListener.onOverScrollTop();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void initTracker(MotionEvent ev) {
        mTracker = VelocityTracker.obtain();
        mTracker.addMovement(ev);
        mYVelocity = 0f;
    }

    public void setOnOverScrollListener(OnOverScrollListener listener) {
        mOverListener = listener;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (mOverListener == null) {
            return;
        }
        if (clampedY) {
            if (mYVelocity > MIN_VELOCITY) {
                mOverListener.onOverScrollTop();
            } else if (mYVelocity < MIN_BOTTOM_VY) {
                mOverListener.onOverScrollBottom();
            }
        }

    }


    private void checkLoadingMore(OverScrollRecyclerView recyclerView) {
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
        if (firstVisibleItems == 0) {
            View child = manager.getChildAt(0);
            if (child != null) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                firstVisible = manager.getChildAt(0).getTop() >= (getPaddingTop() + lp.topMargin);
            }
        }
        // enabling or disabling the refresh layout
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
        setOnScrollListener(new RecyclerScrollListener());
    }

    void checkIfEmpty() {
        if (mEmptyView != null) {
            boolean empty = getAdapter() == null || getAdapter().getItemCount() == 0;
            mEmptyView.setVisibility(!empty ? GONE : VISIBLE);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
    }

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
        checkIfEmpty();
    }

    public void setLoadingView(ProgressView loadingView) {
        mLoadingView = loadingView;
    }

    public void setLoading(boolean loading) {
        if (mLoadingView != null) {
            if (loading) {
                if (mEmptyView != null)
                    mEmptyView.setVisibility(GONE);
                mLoadingView.setVisibility(VISIBLE);
            } else {
                mLoadingView.setVisibility(GONE);
            }
        }
    }

    private class RecyclerScrollListener extends OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            OverScrollRecyclerView rv = (OverScrollRecyclerView) recyclerView;
            if (mMoreListener != null) {
                checkLoadingMore(rv);

            }
        }
    }
}
