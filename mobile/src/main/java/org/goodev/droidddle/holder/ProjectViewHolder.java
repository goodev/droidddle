package org.goodev.droidddle.holder;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.droidddle.R;
import org.goodev.droidddle.pojo.Project;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.GoURLSpan;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ADMIN on 2015/1/2.
 */
public class ProjectViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.user_image)
    SimpleDraweeView mUserImageView;
    @InjectView(R.id.user_name)
    TextView mUserNameView;
    @InjectView(R.id.bucket_name)
    TextView mBucketNameView;
    @InjectView(R.id.bucket_desc)
    TextView mBucketDescView;
    @InjectView(R.id.shot_count)
    TextView mShotCountView;
    Context mContext;
    Project mData;

    public ProjectViewHolder(View view, Context context) {
        super(view);
        mContext = context;
        ButterKnife.inject(this, view);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.launchProjectShots(mContext, mData);
            }
        });

        mUserImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mData.user != null) {
                    UiUtils.launchUser((android.app.Activity) mContext, mData.user);
                }
            }
        });
    }

    public void setData(Project data) {
        mData = data;
        mUserImageView.setImageURI(Uri.parse(data.user.avatarUrl));
        //        Glide.with(mContext).load(data.user.avatarUrl).placeholder(R.drawable.person_image_empty).into(mUserImageView);
        mBucketNameView.setText(data.name);
        String des = data.description;
        //        if (TextUtils.isEmpty(des)) {
        //            mBucketDescView.setVisibility(View.GONE);
        //        } else {
        //            mBucketDescView.setVisibility(View.VISIBLE);
        //        }
        if (!TextUtils.isEmpty(des)) {
            Spanned spannable = GoURLSpan.hackURLSpan(Html.fromHtml(des));
            mBucketDescView.setText(spannable);
            mBucketDescView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            mBucketDescView.setText(des);
        }
        mUserNameView.setText(data.user.name);
        String count = mContext.getResources().getQuantityString(R.plurals.shot_count, data.shotsCount, data.shotsCount);
        mShotCountView.setText(count);
    }
}
