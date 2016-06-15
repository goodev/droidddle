package org.goodev.droidddle.frag.shot;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.holder.UserViewHolder;
import org.goodev.droidddle.pojo.Like;
import org.goodev.droidddle.widget.BaseAdapter;

/**
 * Created by goodev on 2014/12/26.
 */
public class ShotLikeAdapter extends BaseAdapter<Like> {

    public ShotLikeAdapter(Activity context) {
        super(context);
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }


    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.shot_like_item, parent, false);
        return new UserViewHolder(view, mContext);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        Like data = getItem(position);
        UserViewHolder h = (UserViewHolder) holder;
        h.setData(data.user);
    }

}
