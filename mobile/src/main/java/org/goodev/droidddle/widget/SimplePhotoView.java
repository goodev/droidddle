package org.goodev.droidddle.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * A SimpleDraweeView that supports Pinch to zoom and drag
 */
public class SimplePhotoView extends SimpleDraweeView implements View.OnTouchListener {

    private final ScaleGestureDetector mScaleDetector;

    private final ScaleGestureDetector.OnScaleGestureListener mScaleListener;

    private boolean isAutoScale;

    private float mCurrentScale = 1.0f;

    private final Matrix mCurrentMatrix;

    //the center of x-axle
    private float mMidX;

    //the center of y-axle
    private float mMidY;

    private final float[] matrixValues = new float[9];

    private GestureDetector mGestureDetector;
    //set the maximum of zoom, and the median. The minimum is mCurrentScale


    public static float SCALE_MAX = 2.0f;

    public static float SCALE_MID = 1.5f;

    public SimplePhotoView(Context context) {
        this(context, null);
    }

    public SimplePhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimplePhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //initialize ScaleGestureDetector. Capture the
        mScaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                float newScale = mCurrentScale * scaleFactor;
                // LogUtils.d("scaleFactor=" + scaleFactor + ",newScale=" + newScale);
                // Prevent from zooming out more than original
                if (newScale > 1.0f && newScale < SCALE_MAX) {
                    if (mMidX == 0.0f) {
                        mMidX = getWidth() / 2;//from center to zoom
                    }
                    if (mMidY == 0.0f) {
                        mMidY = getHeight() / 2;//from center to zoom
                    }
                    mCurrentScale = newScale;
                    SimplePhotoView.this.postDelayed(new AutoRunableZoom(newScale), 16);
                } else if (newScale > SCALE_MAX) {
                    newScale = SCALE_MAX;
                    mCurrentScale = newScale;
                }

                return true;
            }
        };
        mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
        mCurrentMatrix = new Matrix();
        //initialize GestureDetector, Capture double tap event
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        float newscale = 0.0f;
                        if (mCurrentScale == 1.0f) {
                            newscale = SCALE_MID;
                        } else if (mCurrentScale >= SCALE_MID && mCurrentScale < SCALE_MAX) {
                            newscale = SCALE_MAX;
                        } else if (mCurrentScale >= SCALE_MAX) {
                            newscale = 1.0f;
                        }
                        mCurrentScale = newscale;
                        SimplePhotoView.this.postDelayed(new AutoRunableZoom(newscale), 16);
                        isAutoScale = true;
                        return true;
                    }
                });
        this.setOnTouchListener(this);
    }

    float mLastX, mLastY;

    private boolean isCanDrag;

    private int lastPointerCount;

    private int mTouchSlop;

    private boolean isCheckTopAndBottom = true;

    private boolean isCheckLeftAndRight = true;

    //the heigh and width of the image
    private int bitmapW, bitmapH;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //first pass events to  estureDetector(doubleTap) and ScaleDetector
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        mScaleDetector.onTouchEvent(event);

        float x = 0, y = 0;
        // pointer num count
        final int pointerCount = event.getPointerCount();
        //get more average values of x and y of several tough points
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;
        /**
         * when pointer change ,rest  x and y
         */
        if (pointerCount != lastPointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }

        lastPointerCount = pointerCount;
        RectF rectF = getMatrixRectF();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                float dx = x - mLastX;
                float dy = y - mLastY;
                if (!isCanDrag) {
                    isCanDrag = isCanDrag(dx, dy);
                }
                if (isCanDrag) {
                    if (getDrawable() != null) {
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        //  if it is not wide enough for the screen, then the left and right movement is forbidden.
                        if (rectF.width() < getWidth()) {
                            isCheckLeftAndRight = false;
                        }
                        //  If it is not high enough for the screen, then the up and down movement is forbidden.
                        if (rectF.height() < getHeight()) {
                            isCheckTopAndBottom = false;
                        }
                        mCurrentMatrix.postTranslate(dx, dy);
                        checkMatrixBounds();
                        // Please pay attention here, after Matrix.postTranslate, do not use setImageMatrix(matrix). Use the method of invalidate().
                        // Becuase the drawee will not extend the ImageView, there is no way to make ImageView functional.
                        invalidate();
                    }
                }
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                lastPointerCount = 0;
                break;
        }

        return true;
    }

    /**
     * Drawee top-level-drawable doesn't expose its intrinsic dimensions is because intrinsic
     * dimensions change when the image loads
     * so  they return -1, But when we use RectF, we have to use the width and height of bitmap
     * You can get the width and height in two ways and set
     * Postprocessor : http://frescolib.org/docs/modifying-image.html#_
     * ControllerListener : http://frescolib.org/docs/listening-download-events.html#_
     */
    public void setBitmapWandH(int width, int height) {
        bitmapH = height;
        bitmapW = width;
    }


    /**
     * getDrawable can not be used here. TopLevelDrawable is not a BitmapDrawable getintrinsic
     * return -1
     * Use setBitmapWandH() to get width and height
     */
    private RectF getMatrixRectF() {
        RectF rect = new RectF();
        rect.set(0, 0, bitmapW, bitmapH);
        //real bounds of the image in the view
        getHierarchy().getActualImageBounds(rect);
        Matrix matrix = mCurrentMatrix;
        matrix.mapRect(rect);
        return rect;
    }

    /**
     * when  During the move, the bounded is checked. The width and height which is oversized of the
     * screen is checked.
     */
    private void checkMatrixBounds() {
        RectF rect = getMatrixRectF();
        float deltaX = 0, deltaY = 0;
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();
        //  After the movement and zoom, whether it is over the screen eage is checked.
        if (rect.top > 0 && isCheckTopAndBottom) {
            deltaY = -rect.top;
        }
        if (rect.bottom < viewHeight && isCheckTopAndBottom) {
            deltaY = viewHeight - rect.bottom;
        }
        if (rect.left > 0 && isCheckLeftAndRight) {
            deltaX = -rect.left;
        }
        if (rect.right < viewWidth && isCheckLeftAndRight) {
            deltaX = viewWidth - rect.right;
        }
        mCurrentMatrix.postTranslate(deltaX, deltaY);
    }

    private boolean isCanDrag(float dx, float dy) {
        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
    }


    /**
     * automatically zoom event
     */
    private class AutoRunableZoom implements Runnable {

        // progressive show is better
        static final float BIGGER = 1.07f;

        static final float SMALLER = 0.93f;

        private float mTargetScale;

        private float tmpScale;

        // input the target scale. According to the target scale and the current scale, to become smaller or bigger is checked.
        public AutoRunableZoom(float TargetScale) {
            this.mTargetScale = TargetScale;
            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            } else {
                tmpScale = SMALLER;
            }
            if (mMidX == 0.0f) {
                mMidX = getWidth() / 2;//from center to zoom
            }
            if (mMidY == 0.0f) {
                mMidY = getHeight() / 2;//from center to zoom
            }
        }

        @Override
        public void run() {
            //  zoom in and out
            // please pay attention here. Do not use setImageMatrix(matrix) after Matrix.postScale. Use the method of invalidate()
            //  Because drawee can not extend ImageView, there is no way to use ImageView.
            mCurrentMatrix.postScale(tmpScale, tmpScale, mMidX, mMidY);
            invalidate();
            mCurrentScale = getScale() > SCALE_MAX ? SCALE_MAX
                    : getScale() < 1.0f ? 1.0f : getScale();
            //  If the value is in , keep zooming
            if (((tmpScale > 1f) && (mCurrentScale < mTargetScale)) || ((tmpScale < 1f) && (
                    mTargetScale < mCurrentScale))) {
                SimplePhotoView.this.postDelayed(this, 16);
            } else {
                // Set the target ratio scale
                final float deltaScale = mTargetScale / mCurrentScale;
                mCurrentMatrix.postScale(deltaScale, deltaScale, mMidX, mMidY);
                invalidate();
                isAutoScale = false;
            }
        }
    }


    public final float getScale() {
        mCurrentMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /**
     * Here, you have to ��http://frescolib.org/docs/writing-custom-views.html#_
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        canvas.concat(mCurrentMatrix);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    /**
     * Here, you have to��http://frescolib.org/docs/writing-custom-views.html#_
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScaleDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }


    /**
     * Resets the zoom of the attached image.
     * This has no effect if the image has been destroyed
     */
    public void reset() {
        mCurrentMatrix.reset();
        mCurrentScale = 1.0f;
        invalidate();
    }
}
