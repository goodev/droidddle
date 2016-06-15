package org.goodev.droidddle.frag.user;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.droidddle.R;
import org.goodev.droidddle.pojo.Team;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.BaseAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by goodev on 2014/12/30.
 */
public class UserTeamAdapter extends BaseAdapter<Team> {

    public UserTeamAdapter(Activity context) {
        super(context);
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }


    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.user_team_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Team data = getItem(position);
        Holder h = (Holder) holder;

        h.mUserImageView.setImageURI(Uri.parse(data.avatarUrl));
//        Glide.with(mContext).load(data.avatarUrl).placeholder(R.drawable.person_image_empty).into(h.mUserImageView);
        if (TextUtils.isEmpty(data.location)) {
            h.mLocationView.setText(R.string.default_location);
        } else {
            h.mLocationView.setText(data.location);
        }
        h.mUserNameView.setText(data.name);

        Resources resources = mContext.getResources();
        String shots = resources.getQuantityString(R.plurals.shot_count, data.shotsCount, data.shotsCount);
        h.mShotsCountView.setText(shots);
        String followers = resources.getQuantityString(R.plurals.follower_count, data.followersCount, data.followersCount);
        h.mFollowersCountView.setText(followers);

        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.launchTeam(mContext, data);
            }
        });

    }

    public static class Holder extends RecyclerView.ViewHolder {
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

        public Holder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
