package org.goodev.droidddle.frag.user;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.holder.ShotViewHolder;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.OnOperationListener;

/**
 * Created by goodev on 2014/12/30.
 */
public class UserShotAdapter extends BaseAdapter<Shot> {
    boolean mIsSelf;
    User mUser;
    boolean mIsCleanerMode;
    private OnOperationListener<Shot> mOnOperationListener;

    public UserShotAdapter(Activity context) {
        this(context, null, false);
    }

    public UserShotAdapter(Activity context, User user, boolean isSelf) {
        super(context);
        mIsSelf = isSelf;
        mUser = user;
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
        ShotViewHolder holder = new ShotViewHolder(view, mContext, mIsSelf, mUser);
        holder.setOperationListener(mOnOperationListener);
        holder.setCleanerMode(mIsCleanerMode);
        return holder;
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        Shot shot = getItem(position);
        ShotViewHolder h = (ShotViewHolder) holder;
        h.setData(shot);
    }

    public void setOperationListener(OnOperationListener<Shot> listener) {
        mOnOperationListener = listener;
    }
}