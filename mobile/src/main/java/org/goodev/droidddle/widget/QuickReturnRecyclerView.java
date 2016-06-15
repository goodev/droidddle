package org.goodev.droidddle.widget;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import org.goodev.droidddle.utils.Utils;

/**
 * Add view with BottomToolBarRecyclerView.setReturningView to be used as a QuickReturnView
 * when the user scrolls down the content.
 * <p>
 * Created by johnen on 14-11-11.
 */
public class QuickReturnRecyclerView extends BaseRecyclerView {
    private static final String TAG = QuickReturnRecyclerView.class.getName();
    private static final int STATE_ONSCREEN = 0;
    private int mState = STATE_ONSCREEN;
    private static final int STATE_OFFSCREEN = 1;
    private static final int STATE_RETURNING = 2;
    OnScrollListener mListener1;
    private View mReturningView;
    private int mMinRawY = 0;
    private int mReturningViewHeight;
    private int mGravity = Gravity.BOTTOM;
    private boolean mIsLoadingMore;
    private boolean mHasMoreData = true;
    private OnLoadingMoreListener mMoreListener;

    public QuickReturnRecyclerView(Context context) {
        super(context);
        init();
    }

    public QuickReturnRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QuickReturnRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
    }

    /**
     * The view that should be showed/hidden when scrolling the content.
     * Make sure to set the gravity on the this view to either Gravity.Bottom or
     * Gravity.TOP and to put it preferable in a FrameLayout.
     *
     * @param view Any kind of view
     */
    public void setReturningView(View view) {
        mReturningView = view;

        try {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mReturningView.getLayoutParams();
            mGravity = params.gravity;
        } catch (ClassCastException e) {
            throw new RuntimeException("The return view need to be put in a FrameLayout");
        }

        measureView(mReturningView);
        mReturningViewHeight = mReturningView.getMeasuredHeight();
        setOnScrollListener(new RecyclerScrollListener());
        int left = getPaddingLeft();
        int right = getPaddingRight();
        int top = getPaddingTop() + mReturningViewHeight;
        int bottom = getPaddingBottom();
        setPadding(left, top, right, bottom);
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * if layout manager do not have this method , will return 0
     *
     * @return
     */
    public int findFirstVisibleItemPosition() {
        LayoutManager manager = getLayoutManager();

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

        return firstVisibleItems;
    }

    public void checkLoadingMore() {
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

        //        boolean topOfFirstItemVisible = manager.getChildAt(0).getTop() >= mFirstTop;
        // enabling or disabling the refresh layout
        if (firstVisibleItems == 0) {
            View child = manager.getChildAt(0);
            if (child != null) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                firstVisible = manager.getChildAt(0).getTop() >= (getPaddingTop() + lp.topMargin);
            }
        }
        //        firstVisible = firstVisibleItems == 0 && topOfFirstItemVisible;

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

    public OnLoadingMoreListener getOnLoadingMoreListener() {
        return mMoreListener;
    }

    public void setOnLoadingMoreListener(OnLoadingMoreListener listener) {
        mMoreListener = listener;
        setOnScrollListener(new RecyclerScrollListener());
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        if (mListener1 == null) {
            mListener1 = listener;
        } else if (mListener1 != listener) {
            super.setOnScrollListener(new ScrollListenerWrap(mListener1, listener));
            return;
        }
        super.setOnScrollListener(listener);
    }

    static class ScrollListenerWrap extends OnScrollListener {
        OnScrollListener mListener1;
        OnScrollListener mListener2;

        public ScrollListenerWrap(OnScrollListener listener1, OnScrollListener listener2) {
            mListener1 = listener1;
            mListener2 = listener2;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            mListener1.onScrollStateChanged(recyclerView, newState);
            mListener2.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            mListener1.onScrolled(recyclerView, dx, dy);
            mListener2.onScrolled(recyclerView, dx, dy);
        }
    }

    private class RecyclerScrollListener extends OnScrollListener {
        private int mScrolledY;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            QuickReturnRecyclerView rv = (QuickReturnRecyclerView) recyclerView;
            if (mMoreListener != null) {
                checkLoadingMore();

            }
            if (mReturningView == null) {
                return;
            }

            if (mGravity == Gravity.BOTTOM) {
                mScrolledY += dy;
                if (mScrolledY < 0) {
                    mScrolledY = 0;
                }
            } else if (mGravity == Gravity.TOP) {
                mScrolledY -= dy;
                if (mScrolledY > 0) {
                    mScrolledY = 0;
                }
            }

            //            L.d("dy "+dy +" mScrolledY "+mScrolledY);
            if (mReturningView == null)
                return;

            int translationY = 0;
            int rawY = mScrolledY;

            switch (mState) {
                case STATE_OFFSCREEN:
                    if (mGravity == Gravity.BOTTOM) {
                        if (rawY >= mMinRawY) {
                            mMinRawY = rawY;
                        } else {
                            mState = STATE_RETURNING;
                        }
                    } else if (mGravity == Gravity.TOP) {
                        if (rawY <= mMinRawY) {
                            mMinRawY = rawY;
                        } else {
                            mState = STATE_RETURNING;
                        }
                    }

                    translationY = rawY;
                    break;

                case STATE_ONSCREEN:
                    if (mGravity == Gravity.BOTTOM) {

                        if (rawY > mReturningViewHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                    } else if (mGravity == Gravity.TOP) {

                        if (rawY < -mReturningViewHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                    }
                    translationY = rawY;
                    break;

                case STATE_RETURNING:
                    if (mGravity == Gravity.BOTTOM) {
                        translationY = (rawY - mMinRawY) + mReturningViewHeight;

                        if (translationY < 0) {
                            translationY = 0;
                            mMinRawY = rawY + mReturningViewHeight;
                        }

                        if (rawY == 0) {
                            mState = STATE_ONSCREEN;
                            translationY = 0;
                        }

                        if (translationY > mReturningViewHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                    } else if (mGravity == Gravity.TOP) {
                        translationY = (rawY + Math.abs(mMinRawY)) - mReturningViewHeight;

                        if (translationY > 0) {
                            translationY = 0;
                            mMinRawY = rawY - mReturningViewHeight;
                        }

                        if (rawY == 0) {
                            mState = STATE_ONSCREEN;
                            translationY = 0;
                        }

                        if (translationY < -mReturningViewHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                    }
                    break;
            }

            /** this can be used if the build is below honeycomb **/
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
                TranslateAnimation anim = new TranslateAnimation(0, 0, translationY, translationY);
                anim.setFillAfter(true);
                anim.setDuration(0);
                mReturningView.startAnimation(anim);
            } else {
                mReturningView.setTranslationY(translationY);
            }

        }
    }
}
