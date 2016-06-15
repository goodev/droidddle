package org.goodev.droidddle;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.goodev.droidddle.frag.shot.ShotAttachmentFragment;
import org.goodev.droidddle.frag.shot.ShotBucketFragment;
import org.goodev.droidddle.frag.shot.ShotCommentFragment;
import org.goodev.droidddle.frag.shot.ShotLikeFragment;
import org.goodev.droidddle.frag.shot.ShotProjectFragment;
import org.goodev.droidddle.frag.shot.ShotReboundFragment;
import org.goodev.droidddle.pojo.Shot;

import java.util.ArrayList;

/**
 * Created by goodev on 2015/1/14.
 */
public class ShotDetailsPagerAdapter extends FragmentStatePagerAdapter {
    private static final int TYPE_COMMENTS = 1;
    private static final int TYPE_ATTACHMENTS = 2;
    private static final int TYPE_LIKES = 3;
    private static final int TYPE_BUCKETS = 4;
    private static final int TYPE_REBOUND = 5;
    private static final int TYPE_PROJECTS = 6;
    Shot mShot;
    ArrayList<Integer> mItemsId;
    Context mContext;
    boolean mIsSelf;

    public ShotDetailsPagerAdapter(Context context, FragmentManager fm, Shot shot, boolean isSelf) {
        super(fm);
        mContext = context;
        mShot = shot;
        mIsSelf = isSelf;
        mItemsId = new ArrayList<>();
        setupItems();
    }

    private void setupItems() {
        mItemsId.add(TYPE_COMMENTS);
        if (mShot.attachmentsCount > 0) {
            mItemsId.add(TYPE_ATTACHMENTS);
        }
        if (mShot.likesCount > 0) {
            mItemsId.add(TYPE_LIKES);
        }
        if (mShot.bucketsCount > 0) {
            mItemsId.add(TYPE_BUCKETS);
        }
        if (mShot.reboundsCount > 0) {
            mItemsId.add(TYPE_REBOUND);
        }
        mItemsId.add(TYPE_PROJECTS);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int resId = 0;
        int count = -1;
        switch (mItemsId.get(position).intValue()) {
            case TYPE_COMMENTS:
                resId = R.string.comments_text;
                count = mShot.commentsCount;
                break;
            case TYPE_ATTACHMENTS:
                resId = R.string.attachment_text;
                count = mShot.attachmentsCount;
                break;
            case TYPE_LIKES:
                resId = R.string.likes_text;
                count = mShot.likesCount;
                break;
            case TYPE_BUCKETS:
                resId = R.string.buckets_text;
                count = mShot.bucketsCount;
                break;
            case TYPE_REBOUND:
                resId = R.string.rebounds_text;
                count = mShot.reboundsCount;
                break;
            case TYPE_PROJECTS:
                resId = R.string.projects_text;
                count = -1;
                break;
        }
        if (resId == 0) {
            return null;
        }
        if (count == -1) {
            return mContext.getResources().getString(resId);
        }
        return mContext.getResources().getString(resId, count);
    }

    @Override
    public Fragment getItem(int position) {
        switch (mItemsId.get(position).intValue()) {
            case TYPE_COMMENTS:
                return ShotCommentFragment.newInstance(mShot.id, mContext.getResources().getColor(R.color.primary_color));
            case TYPE_ATTACHMENTS:
                return ShotAttachmentFragment.newInstance(mShot.id, mIsSelf);
            case TYPE_LIKES:
                return ShotLikeFragment.newInstance(mShot.id, null);
            case TYPE_BUCKETS:
                return ShotBucketFragment.newInstance(mShot.id, mShot.bucketsCount);
            case TYPE_REBOUND:
                return ShotReboundFragment.newInstance(mShot.id, mShot.reboundsCount);
            case TYPE_PROJECTS:
                return ShotProjectFragment.newInstance(mShot.id, 0);
        }

        return null;
    }

    @Override
    public int getCount() {
        return mItemsId.size();
    }
}
