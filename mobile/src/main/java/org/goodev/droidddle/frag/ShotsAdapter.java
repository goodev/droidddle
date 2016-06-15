package org.goodev.droidddle.frag;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import org.goodev.droidddle.holder.ShotAdViewHolder;
import org.goodev.droidddle.holder.ShotViewHolder;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.widget.BaseShotsAdapter;

/**
 * Created by goodev on 2015/1/8.
 */
public class ShotsAdapter extends BaseShotsAdapter {
    private boolean mFromSearch;

    public ShotsAdapter(Activity context) {
        this(context, false);
    }

    public ShotsAdapter(Activity context, boolean search) {
        super(context);
        mFromSearch = search;
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        Shot data = getItem(position);
        if (holder instanceof ShotViewHolder) {
            ((ShotViewHolder) holder).setData(data, mFromSearch);
        } else if (holder instanceof ShotAdViewHolder) {
            ((ShotAdViewHolder) holder).setData(data);
        }
    }
}
