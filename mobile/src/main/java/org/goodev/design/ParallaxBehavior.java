package org.goodev.design;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yfcheng on 2015/9/15.
 */
public class ParallaxBehavior extends CoordinatorLayout.Behavior<View> {

    public ParallaxBehavior(Context context, AttributeSet attrs) {
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
//        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
//        child.setTranslationY(translationY);
        child.setTranslationY(dependency.getScrollY() / 2);
        return true;
    }
}