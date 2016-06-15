package org.goodev.droidddle.frag.user;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.goodev.droidddle.R;
import org.goodev.droidddle.holder.OnClickListener;
import org.goodev.droidddle.pojo.Bucket;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.OnOperationListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by goodev on 2014/12/30.
 */
public class UserBucketAdapter extends BaseAdapter<Bucket> {
    OnClickListener<Bucket> mBucketListener;
    boolean mIsSelf;
    private OnOperationListener<Bucket> mOperationListener;

    public UserBucketAdapter(Activity context, OnClickListener<Bucket> listener) {
        this(context, listener, false);
    }

    public UserBucketAdapter(Activity context, OnClickListener<Bucket> listener, boolean self) {
        super(context);
        mIsSelf = self;
        mBucketListener = listener;
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.user_bucket_item, parent, false);
        Holder holder = new Holder(view, mContext);
        holder.setOperationListener(mOperationListener);
        holder.setIsSelf(mIsSelf);
        holder.setBucketListener(mBucketListener);
        return holder;
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        Bucket data = getItem(position);
        Holder h = (Holder) holder;

        h.setData(data, position);
    }

    public void setOperationListener(OnOperationListener<Bucket> listener) {
        mOperationListener = listener;
    }

    public static class Holder extends RecyclerView.ViewHolder {
        @InjectView(R.id.bucket_name)
        TextView mBucketNameView;
        @InjectView(R.id.bucket_desc)
        TextView mBucketDescView;
        @InjectView(R.id.shot_count)
        TextView mShotCountView;

        @InjectView(R.id.swipe_front)
        View mFrontView;
        @InjectView(R.id.more_menu)
        View mMoreMenuView;

        Activity mContext;
        boolean mIsSelf;

        private OnOperationListener<Bucket> mOperationListener;
        private OnClickListener<Bucket> mBucketListener;

        public Holder(View view, Activity context) {
            super(view);
            mContext = context;
            ButterKnife.inject(this, view);
        }

        public void setOperationListener(OnOperationListener<Bucket> listener) {
            mOperationListener = listener;
        }

        public void setIsSelf(boolean self) {
            mIsSelf = self;
        }

        public void setData(Bucket data, int position) {
            mBucketNameView.setText(data.name);
            String des = data.description;
            if (TextUtils.isEmpty(des)) {
                mBucketNameView.setGravity(Gravity.CENTER_VERTICAL);
                mBucketDescView.setVisibility(View.GONE);
                mBucketDescView.setText(des);
            } else {
                mBucketNameView.setGravity(Gravity.BOTTOM);
                mBucketDescView.setVisibility(View.VISIBLE);
                //DO not handle links in des
                //                Spanned spannable = GoURLSpan.hackURLSpan(Html.fromHtml(des));
                mBucketDescView.setText(Html.fromHtml(des));
                //                mBucketDescView.setMovementMethod(LinkMovementMethod.getInstance());
            }
            String count = mContext.getResources().getQuantityString(R.plurals.shot_count, data.shotsCount, data.shotsCount);
            mShotCountView.setText(count);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBucketListener != null) {
                        mBucketListener.onClicked(data);
                    } else {
                        UiUtils.launchBucketShots(mContext, data, mIsSelf);
                    }
                }
            });

            if (mIsSelf && mBucketListener == null) {
                UiUtils.setupOverflowEditMenu(mContext, mMoreMenuView, mOperationListener, data, position);
                mMoreMenuView.setVisibility(View.VISIBLE);
            } else {
                mMoreMenuView.setVisibility(View.GONE);
            }
        }

        public void setBucketListener(OnClickListener<Bucket> listener) {
            mBucketListener = listener;
        }
    }
}
