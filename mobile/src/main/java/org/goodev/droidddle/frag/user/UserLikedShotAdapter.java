package org.goodev.droidddle.frag.user;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.holder.OnUnlikeShotListener;
import org.goodev.droidddle.holder.ShotViewHolder;
import org.goodev.droidddle.pojo.LikedShot;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.widget.BaseAdapter;

/**
 * Created by goodev on 2014/12/30.
 */
public class UserLikedShotAdapter extends BaseAdapter<LikedShot> implements OnUnlikeShotListener {

    private boolean mIsSelf;
    private boolean mIsCleanerMode;

    public UserLikedShotAdapter(Activity context) {
        this(context, false);
    }

    public UserLikedShotAdapter(Activity context, boolean isSelf) {
        super(context);
        mIsSelf = isSelf;
        mIsCleanerMode = Pref.isCleanerMode(context);
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        int layoutRes = mIsCleanerMode ? R.layout.user_shot_item_cleaner : R.layout.user_shot_item;
        final View view = LayoutInflater.from(mContext).inflate(layoutRes, parent, false);
        ShotViewHolder holder = new ShotViewHolder(view, mContext, mIsSelf, this);
        holder.setCleanerMode(mIsCleanerMode);

        return holder;
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        LikedShot data = getItem(position);
        Shot shot = data.shot;
        ShotViewHolder h = (ShotViewHolder) holder;
        h.setData(shot, position);
    }

    @Override
    public void onUnlikeShot(int position) {
        if (mIsSelf) {
            removeData(position);
        }
    }
}
