package org.goodev.droidddle;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.ThemeUtil;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.SlidingTabLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by goodev on 2015/1/15.
 */
public abstract class DetailsActivity<T extends Parcelable> extends UpActivity {
    protected static final int SLID_PAGER_TIME = 250;
    protected static final int ACTIONBAR_BG_TIME = 100;
    protected static final int ANIMATION_STATUS_BAR_COLOR_CHANGE_DURATION = 150;
    protected int mActionBarSize;
    protected float mPreActionBarBackgroundAlpha;
    /**
     * large width device
     */
    protected boolean mHasTwoPane;
    protected PagerAdapter mPagerAdapter;
    int mPrimaryColor;

    @Optional
    @InjectView(R.id.tabs)
    SlidingTabLayout mTabs;
    @Optional
    @InjectView(R.id.pager)
    ViewPager mViewPager;
    @InjectView(R.id.scroll_wrapper)
    FrameLayout mInterceptionLayout;
    T mData;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        mPrimaryColor = ThemeUtil.getThemeColor(this, R.attr.colorPrimary);
        setContentView(getLayoutRes());
        // Postpone the shared element enter transition.
//        supportPostponeEnterTransition();
        getSupportFragmentManager().executePendingTransactions();
        initParams();
        mHasTwoPane = getResources().getBoolean(R.bool.two_pane);

        ButterKnife.inject(this);
        mActionBarSize = getActionBarSize();
        if (mHasTwoPane) {
            ViewGroup root = (ViewGroup) findViewById(R.id.main_content);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) root
                    .getLayoutParams();
            params.topMargin = 0;
        }
    }

    /**
     * Schedules the shared element transition to be started immediately
     * after the shared element has been measured and laid out within the
     * activity's view hierarchy. Some common places where it might make
     * sense to call this method are:
     * <p>
     * (1) Inside a Fragment's onCreateView() method (if the shared element
     * lives inside a Fragment hosted by the called Activity).
     * <p>
     * (2) Inside a Picasso Callback object (if you need to wait for Picasso to
     * asynchronously load/scale a bitmap before the transition can begin).
     * <p>
     * (3) Inside a LoaderCallback's onLoadFinished() method (if the shared
     * element depends on data queried by a Loader).
     */
    public void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                });
    }

    @Override
    protected void onPostMyCreate(Bundle savedInstanceState) {
        if (!mHasTwoPane) {
            setSupportActionBar(mToolbar);

            ViewTreeObserver vto = mInterceptionLayout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mInterceptionLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mInterceptionLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    mInterceptionLayout.setTranslationY(getScreenHeight());
                }
            });

            if (Pref.isShowDetailsAd(this)) {
                int type = UiUtils.getAdsBannerType(this);
            }
        } else {
            if (mData != null) {
                addViewPager();
            }
        }
    }


    /**
     * set activity layout
     */
    protected abstract int getLayoutRes();

    protected abstract void initParams();

    protected int getBastLayout() {
        return R.layout.detalis_layout_base;
    }

    protected void addViewPager() {
        mPagerAdapter = getViewPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
        mTabs.setViewPager(mViewPager);
        mTabs.postInvalidate();
    }

    protected abstract PagerAdapter getViewPagerAdapter();

    protected int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    protected int getScreenHeight() {
        return findViewById(android.R.id.content).getHeight();
    }

    protected void slidDownTabs() {
        if (mToolbar != null) {
            mToolbar.setSubtitle(null);
        }
        float end = getScreenHeight();
        float start = mInterceptionLayout.getTranslationY();
        ObjectAnimator animator = ObjectAnimator.ofFloat(mInterceptionLayout, "translationY", start, end);
        animator.setDuration(SLID_PAGER_TIME);
        animator.start();
    }

    protected boolean needSlidDownTabs() {
        float translationY = mInterceptionLayout.getTranslationY();
        return getScreenHeight() > translationY;
    }

    protected void scrollToTop() {
        float end = 0;
        float start = mInterceptionLayout.getTranslationY();
        ObjectAnimator animator = ObjectAnimator.ofFloat(mInterceptionLayout, "translationY", start, end);
        animator.setDuration(SLID_PAGER_TIME);
        animator.start();
    }

    @Override
    public void onBackPressed() {
        if (!mHasTwoPane && needSlidDownTabs()) {
            slidDownTabs();
            return;
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.sliding_content);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.home:
            case android.R.id.home:
                if (needSlidDownTabs()) {
                    slidDownTabs();
                    return true;
                }
                supportFinishAfterTransition();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
