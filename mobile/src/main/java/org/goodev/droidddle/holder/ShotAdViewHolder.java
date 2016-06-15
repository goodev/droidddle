package org.goodev.droidddle.holder;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.droidddle.R;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.Ads;
import org.goodev.droidddle.utils.FrescoUtils;
import org.goodev.droidddle.utils.L;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ADMIN on 2015/12/18.
 */
public class ShotAdViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.user_image)
    SimpleDraweeView mUserImageView;

    @InjectView(R.id.shot_title)
    TextView mShotTitleView;

    @InjectView(R.id.shot_views)
    RatingBar mShotViewsView;
    @InjectView(R.id.shot_description)
    TextView mShotDescriptionView;
    @InjectView(R.id.shot_comment)
    TextView mShotComment;
    @InjectView(R.id.shot_image)
    SimpleDraweeView mShotImageView;

    private Activity mContext;

    public ShotAdViewHolder(View view, Activity context) {
        super(view);
        mContext = context;
        ButterKnife.inject(this, view);

        FrescoUtils.setShotAdHierarchy(mContext, mShotImageView);
    }


    public SimpleDraweeView getShotView() {
        return mShotImageView;
    }

    public void setData(Shot shot) {
        L.d("calzz adxmi url%s", shot.reboundsUrl);
        FrescoUtils.setShotUrl(mShotImageView, shot.reboundsUrl, null);
        mUserImageView.setImageURI(Uri.parse(shot.commentsUrl));

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ads.onClickAd(mContext, shot);
            }
        });


        mShotTitleView.setText(shot.title);
        mShotComment.setText(TextUtils.concat(shot.projectsUrl, "  ", shot.attachmentsUrl));
        mShotViewsView.setRating(shot.likesCount / 100f);

        if (TextUtils.isEmpty(shot.description)) {
            mShotDescriptionView.setText(null);
            mShotDescriptionView.setVisibility(View.INVISIBLE);
        } else {
            mShotDescriptionView.setText(shot.description);
        }


    }

}
