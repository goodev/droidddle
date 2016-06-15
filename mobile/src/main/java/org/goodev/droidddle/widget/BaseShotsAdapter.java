package org.goodev.droidddle.widget;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.holder.ShotAdViewHolder;
import org.goodev.droidddle.holder.ShotViewHolder;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.Ads;
import org.goodev.droidddle.utils.Pref;

/**
 * Created by ADMIN on 2015/2/4.
 */
public abstract class BaseShotsAdapter extends BaseAdapter<Shot> {

    protected boolean mIsCleanerMode;
    public static final int TYPE_AD = 2;

    public BaseShotsAdapter(Activity context) {
        super(context);
        mIsCleanerMode = Pref.isCleanerMode(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (mHasLoading && mDataList.size() == position) {
            return TYPE_LOADING;
        }
        Shot shot = getItem(position);
        if (Ads.isAd(shot.id)) {
            return TYPE_AD;
        }
        return TYPE_SHOT;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_AD) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.shot_ad_item, parent, false);
            ShotAdViewHolder holder = new ShotAdViewHolder(view, mContext);
            return holder;
        }
        int layoutRes = mIsCleanerMode ? R.layout.shot_item_cleaner : R.layout.shot_item;
        final View view = LayoutInflater.from(mContext).inflate(layoutRes, parent, false);
        ShotViewHolder holder = new ShotViewHolder(view, mContext);
        holder.setCleanerMode(mIsCleanerMode);
        return holder;
    }

//    //Item aniamtion
//    private int mDuration = 300;
//    private Interpolator mInterpolator = new DecelerateInterpolator();
//    private int mLastPosition = -1;
//
//    @Override
//    public void setData(List<Shot> shots) {
//        super.setData(shots);
//        mLastPosition = -1;
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        super.onBindViewHolder(holder, position);
//
//        if (position > mLastPosition) {
//            Animator anim = ObjectAnimator.ofFloat(holder.itemView, "translationY", holder.itemView.getMeasuredHeight(), 0);
//            anim.setDuration(mDuration);
//            anim.setInterpolator(mInterpolator);
//            anim.start();
//            mLastPosition = position;
//        } else {
//            clear(holder.itemView);
//        }
//    }
//
//    protected Animator[] getAnimators(View view) {
//        return new Animator[]{
//                ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0)
//        };
//    }
//
//    public static void clear(View v) {
////        ViewCompat.setAlpha(v, 1);
////        ViewCompat.setScaleY(v, 1);
////        ViewCompat.setScaleX(v, 1);
//        ViewCompat.setTranslationY(v, 0);
//        ViewCompat.setTranslationX(v, 0);
////        ViewCompat.setRotation(v, 0);
////        ViewCompat.setRotationY(v, 0);
////        ViewCompat.setRotationX(v, 0);
//        // @TODO https://code.google.com/p/android/issues/detail?id=80863
//        // ViewCompat.setPivotY(v, v.getMeasuredHeight() / 2);
////        v.setPivotY(v.getMeasuredHeight() / 2);
////        ViewCompat.setPivotX(v, v.getMeasuredWidth() / 2);
//        ViewCompat.animate(v).setInterpolator(null);
//    }
}
