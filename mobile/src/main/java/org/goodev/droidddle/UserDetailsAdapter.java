package org.goodev.droidddle;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.goodev.droidddle.frag.user.UserBucketFragment;
import org.goodev.droidddle.frag.user.UserFollowerFragment;
import org.goodev.droidddle.frag.user.UserFollowingFragment;
import org.goodev.droidddle.frag.user.UserLikedShotFragment;
import org.goodev.droidddle.frag.user.UserProjectFragment;
import org.goodev.droidddle.frag.user.UserShotFragment;
import org.goodev.droidddle.frag.user.UserTeamFragment;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.UiUtils;

import java.util.ArrayList;

/**
 * Created by goodev on 2015/1/14.
 */
public class UserDetailsAdapter extends FragmentStatePagerAdapter {
    private static final int TYPE_SHOTS = 1;
    private static final int TYPE_PROJECTS = 2;
    private static final int TYPE_FOLLOWERS = 3;
    private static final int TYPE_FOLLOWINGS = 4;
    private static final int TYPE_BUCKETS = 5;
    private static final int TYPE_TEAMS = 6;
    private static final int TYPE_LIKES = 7;
    User mShot;
    ArrayList<Integer> mItemsId;
    Context mContext;
    private int mScrollY;

    public UserDetailsAdapter(Context context, FragmentManager fm, User shot) {
        super(fm);
        mContext = context;
        mShot = shot;
        mItemsId = new ArrayList<>();
        setupItems();
    }

    public void setScrollY(int scrollY) {
        mScrollY = scrollY;
    }

    private void setupItems() {
        if (mShot.shotsCount != null && mShot.shotsCount > 0) {
            mItemsId.add(TYPE_SHOTS);
        }
        if (mShot.projectsCount != null && mShot.projectsCount > 0) {
            mItemsId.add(TYPE_PROJECTS);
        }
        if (mShot.followersCount != null && mShot.followersCount > 0) {
            mItemsId.add(TYPE_FOLLOWERS);
        }
        if (mShot.followingsCount != null && mShot.followingsCount > 0) {
            mItemsId.add(TYPE_FOLLOWINGS);
        }

        if (mShot.bucketsCount != null && mShot.bucketsCount > 0) {
            mItemsId.add(TYPE_BUCKETS);
        }
        if (mShot.teamsCount != null && mShot.teamsCount > 0) {
            mItemsId.add(TYPE_TEAMS);
        }
        if (mShot.likesCount != null && mShot.likesCount > 0) {
            mItemsId.add(TYPE_LIKES);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int resId = 0;
        int count = -1;
        switch (mItemsId.get(position).intValue()) {
            case TYPE_SHOTS:
                resId = R.string.shots_text;
                count = UiUtils.getCountValue(mShot.shotsCount);
                break;
            case TYPE_FOLLOWERS:
                resId = R.string.followers_text;
                count = UiUtils.getCountValue(mShot.followersCount);
                break;
            case TYPE_FOLLOWINGS:
                resId = R.string.followings_text;
                count = UiUtils.getCountValue(mShot.followingsCount);
                break;
            case TYPE_LIKES:
                resId = R.string.likes_text;
                count = UiUtils.getCountValue(mShot.likesCount);
                break;
            case TYPE_BUCKETS:
                resId = R.string.buckets_text;
                count = UiUtils.getCountValue(mShot.bucketsCount);
                break;
            case TYPE_TEAMS:
                resId = R.string.teams_text;
                count = UiUtils.getCountValue(mShot.teamsCount);
                break;
            case TYPE_PROJECTS:
                resId = R.string.projects_text_with_number;
                count = UiUtils.getCountValue(mShot.projectsCount);
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
            case TYPE_SHOTS:
                return UserShotFragment.newInstance(mShot);
            case TYPE_FOLLOWERS:
                return UserFollowerFragment.newInstance(mShot);
            case TYPE_LIKES:
                return UserLikedShotFragment.newInstance(mShot);
            case TYPE_BUCKETS:
                return UserBucketFragment.newInstance(mShot);
            case TYPE_TEAMS:
                return UserTeamFragment.newInstance(mShot);
            case TYPE_PROJECTS:
                return UserProjectFragment.newInstance(mShot);
            case TYPE_FOLLOWINGS:
                return UserFollowingFragment.newInstance(mShot);
        }

        return null;
    }


    @Override
    public int getCount() {
        return mItemsId.size();
    }
}
