package org.goodev.droidddle.frag;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.holder.ShotViewHolder;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.widget.BaseShotsAdapter;
import org.goodev.droidddle.widget.OnOperationListener;

/**
 * Created by goodev on 2014/12/31.
 */
public class ProjectShotAdapter extends BaseShotsAdapter {

    boolean mIsSelf;
    boolean mIsBucket;
    OnOperationListener<Shot> mOperationListener;

    public ProjectShotAdapter(Activity context) {
        super(context);
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        int layoutRes = mIsCleanerMode ? R.layout.shot_item_cleaner : R.layout.shot_item;
        final View view = LayoutInflater.from(mContext).inflate(layoutRes, parent, false);
        ShotViewHolder h = new ShotViewHolder(view, mContext);
        h.setIsSelf(mIsSelf);
        h.setIsBucket(mIsBucket);
        h.setCleanerMode(mIsCleanerMode);
        h.setOperationListener(mOperationListener);
        return h;
    }

    public void setOperationListener(OnOperationListener<Shot> listener) {
        mOperationListener = listener;
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        Shot shot = getItem(position);
        ShotViewHolder h = (ShotViewHolder) holder;
        h.setData(shot, position);
    }

    public void setIsSelf(boolean isSelf) {
        mIsSelf = isSelf;
    }

    public void setIsBucket(boolean isBucket) {
        mIsBucket = isBucket;
    }
}
