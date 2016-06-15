package org.goodev.droidddle.frag.team;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import org.goodev.droidddle.holder.ShotViewHolder;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.widget.BaseShotsAdapter;

/**
 * Created by goodev on 2014/12/30.
 */
public class TeamShotAdapter extends BaseShotsAdapter {
    public TeamShotAdapter(Activity context) {
        super(context);

    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }


    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        Shot shot = getItem(position);
        ShotViewHolder h = (ShotViewHolder) holder;
        h.setData(shot);
    }

}
