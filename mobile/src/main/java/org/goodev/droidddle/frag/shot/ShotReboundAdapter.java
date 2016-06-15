package org.goodev.droidddle.frag.shot;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import org.goodev.droidddle.holder.ShotViewHolder;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.widget.BaseShotsAdapter;

/**
 * Created by ADMIN on 2014/12/28.
 */
public class ShotReboundAdapter extends BaseShotsAdapter {
    public ShotReboundAdapter(Activity context) {
        super(context);
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }


    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Shot shot = getItem(position);
        ShotViewHolder h = (ShotViewHolder) holder;
        h.setData(shot);
    }
}
