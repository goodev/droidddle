package org.goodev.droidddle.widget;

import android.view.View;

/**
 * Handler ActionBar background parallax effect
 * Created by goodev on 2014/12/25.
 */
public interface ParallaxScrollListener {

    void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging, View parallaxView, int parallaxHeight);
}
