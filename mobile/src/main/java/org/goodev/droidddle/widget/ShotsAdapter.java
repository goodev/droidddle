package org.goodev.droidddle.widget;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.holder.ShotViewHolder;
import org.goodev.droidddle.pojo.Shot;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by goodev on 2014/12/23.
 */
public class ShotsAdapter extends RecyclerView.Adapter {

    private static final int TYPE_SHOT = 0;
    private static final int TYPE_LOADING = 1;
    Activity mContext;
    List<Shot> mShots;
    private boolean mHasLoading;

    public ShotsAdapter(Activity context) {
        mContext = context;
        mShots = new ArrayList<Shot>();
        setHasStableIds(true);
    }

    public void setLoading(boolean loading) {
        if (mHasLoading == loading) {
            return;
        }
        mHasLoading = loading;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (mHasLoading && mShots.size() == position) {
            return TYPE_LOADING;
        }
        return TYPE_SHOT;
    }

    public void addShots(List<Shot> shots) {
        mShots.addAll(shots);
        notifyDataSetChanged();
    }

    public List<Shot> getShots() {
        return mShots;
    }

    public void setShots(List<Shot> shots) {
        mShots.clear();
        mShots.addAll(shots);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.loading_view, parent, false);
            return new LoadingViewHolder(view);
        } else {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.shot_item, parent, false);
            return new ShotViewHolder(view, mContext);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder h = (LoadingViewHolder) holder;
            h.mProgressView.start();
            return;
        }
        ShotViewHolder h = (ShotViewHolder) holder;
        final Shot shot = mShots.get(position);

        h.setData(shot);
    }

    @Override
    public long getItemId(int position) {
        if (mHasLoading && mShots.size() == position) {
            return -1;
        }
        return mShots.get(position).id;
    }

    @Override
    public int getItemCount() {
        if (mHasLoading) {
            return mShots.size() + 1;
        }
        return mShots.size();
    }


    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.progress)
        ProgressView mProgressView;

        public LoadingViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }


}
