package org.goodev.droidddle.frag.shot;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.holder.BucketViewHolder;
import org.goodev.droidddle.pojo.Bucket;
import org.goodev.droidddle.widget.BaseAdapter;

/**
 * Created by ADMIN on 2014/12/28.
 */
public class ShotBucketAdapter extends BaseAdapter<Bucket> {

    public ShotBucketAdapter(Activity context) {
        super(context);
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }


    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.shot_bucket_item, parent, false);
        return new BucketViewHolder(view, mContext);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        Bucket data = getItem(position);
        BucketViewHolder h = (BucketViewHolder) holder;
        h.setData(data);
    }

}