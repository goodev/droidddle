package org.goodev.droidddle.frag.shot;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.holder.ProjectViewHolder;
import org.goodev.droidddle.pojo.Project;
import org.goodev.droidddle.widget.BaseAdapter;

/**
 * Created by ADMIN on 2014/12/28.
 */
public class ShotProjectAdapter extends BaseAdapter<Project> {

    public ShotProjectAdapter(Activity context) {
        super(context);
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }


    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.shot_bucket_item, parent, false);
        return new ProjectViewHolder(view, mContext);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        Project data = getItem(position);
        ProjectViewHolder h = (ProjectViewHolder) holder;
        h.setData(data);


    }
}