package org.goodev.droidddle.frag.user;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.goodev.droidddle.R;
import org.goodev.droidddle.pojo.Project;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.GoURLSpan;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by goodev on 2014/12/30.
 */
public class UserProjectAdapter extends BaseAdapter<Project> {
    public UserProjectAdapter(Activity context) {
        super(context);
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.user_project_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        Project data = getItem(position);
        Holder h = (Holder) holder;

        h.mBucketNameView.setText(data.name);
        String des = data.description;
        if (TextUtils.isEmpty(des)) {
            h.mBucketNameView.setGravity(Gravity.CENTER_VERTICAL);
            h.mBucketDescView.setVisibility(View.GONE);
            h.mBucketDescView.setText(des);
        } else {
            h.mBucketNameView.setGravity(Gravity.BOTTOM);
            h.mBucketDescView.setVisibility(View.VISIBLE);
            Spanned spannable = GoURLSpan.hackURLSpan(Html.fromHtml(des));
            h.mBucketDescView.setText(spannable);
            h.mBucketDescView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        String count = mContext.getResources().getQuantityString(R.plurals.shot_count, data.shotsCount, data.shotsCount);
        h.mShotCountView.setText(count);

        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.launchProjectShots(mContext, data);
            }
        });


    }

    public static class Holder extends RecyclerView.ViewHolder {
        @InjectView(R.id.bucket_name)
        TextView mBucketNameView;
        @InjectView(R.id.bucket_desc)
        TextView mBucketDescView;
        @InjectView(R.id.shot_count)
        TextView mShotCountView;

        public Holder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
