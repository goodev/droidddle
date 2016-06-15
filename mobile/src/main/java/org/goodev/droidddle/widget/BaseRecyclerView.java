package org.goodev.droidddle.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;

/**
 * Created by ADMIN on 2015/1/1.
 */
public class BaseRecyclerView extends RecyclerView {
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

    public BaseRecyclerView(Context context) {
        super(context);
    }

    public BaseRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

    private void hideLoading() {
        ViewCompat.setAlpha(this, 0f);
        ViewCompat.setTranslationY(this, getMeasuredHeight() / 2);
        ViewCompat.animate(mLoadingView).alpha(0f).translationYBy(-mLoadingView.getHeight()).setDuration(400)
                .scaleX(0.6f)
                .scaleY(0.6f)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        super.onAnimationEnd(view);
                        view.setVisibility(GONE);
                        ViewCompat.setAlpha(view, 1f);
                        ViewCompat.setTranslationY(view, 0);
                        ViewCompat.setScaleX(view, 1f);
                        ViewCompat.setScaleY(view, 1f);
                    }
                })
                .start();
        ViewCompat.animate(this).alpha(1f).translationY(0).setDuration(500)
                .setStartDelay(200).setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(View view) {
                super.onAnimationEnd(view);
                ViewCompat.setAlpha(view, 1f);
                ViewCompat.setTranslationY(view, 0);
            }
        }).start();
    }

    public void setLoading(boolean loading, boolean first) {
        if (mLoadingView != null) {
            if (loading) {
                if (mEmptyView != null)
                    mEmptyView.setVisibility(GONE);
                mLoadingView.setVisibility(VISIBLE);
            } else if (mLoadingView.getVisibility() == VISIBLE) {
                if (first) {
                    hideLoading();
                } else {
                    mLoadingView.setVisibility(GONE);
                }
            }
        }
    }

    @Override
    protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {

        if (getAdapter() != null && false) {
            LayoutManager manager = getLayoutManager();

            GridLayoutAnimationController.AnimationParameters animationParams =
                    (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;

            if (animationParams == null) {
                animationParams = new GridLayoutAnimationController.AnimationParameters();
                params.layoutAnimationParameters = animationParams;
            }

            int columns = 1;
            if (manager instanceof GridLayoutManager) {
                columns = ((GridLayoutManager) manager).getSpanCount();
            }

            animationParams.count = count;
            animationParams.index = index;
            animationParams.columnsCount = columns;
            animationParams.rowsCount = count / columns;

            final int invertedIndex = count - 1 - index;
            animationParams.column = columns - 1 - (invertedIndex % columns);
            animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns;

        } else {
            super.attachLayoutAnimationParameters(child, params, index, count);
        }
    }
}
