package org.goodev.droidddle.holder;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.droidddle.R;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.UiUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ADMIN on 2015/1/2.
 */
public class UserViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.user_image)
    SimpleDraweeView mUserImageView;
    @InjectView(R.id.user_name)
    TextView mUserNameView;
    @InjectView(R.id.user_location)
    TextView mLocationView;
    @InjectView(R.id.user_shots)
    TextView mShotsCountView;
    @InjectView(R.id.user_followers)
    TextView mFollowersCountView;

    Activity mContext;

    public UserViewHolder(View view, Activity context) {
        super(view);
        mContext = context;
        ButterKnife.inject(this, view);
    }

    public void setData(User user) {
        setData(user, false);
    }

    public void setData(User user, boolean search) {
        mUserImageView.setImageURI(Uri.parse(user.avatarUrl));
        //        Glide.with(mContext).load(user.avatarUrl).placeholder(R.drawable.person_image_empty).into(mUserImageView);
        if (TextUtils.isEmpty(user.location)) {
            mLocationView.setText(R.string.default_location);
        } else {
            mLocationView.setText(user.location);
        }
        mUserNameView.setText(user.name);

        Resources resources = mContext.getResources();
        int shotsCount = user.shotsCount;
        String shots = resources.getQuantityString(R.plurals.shot_count, shotsCount, shotsCount);
        mShotsCountView.setText(shots);
        int followersCount = user.followersCount;
        String followers = resources.getQuantityString(R.plurals.follower_count, followersCount, followersCount);
        mFollowersCountView.setText(followers);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search) {
                    UiUtils.launchUser(mContext, user.username, mUserImageView);
                } else {
                    UiUtils.launchUser(mContext, user, mUserImageView);
                }
            }
        });
    }
}
