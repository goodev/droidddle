package org.goodev.droidddle;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.goodev.droidddle.frag.team.TeamMemberFragment;
import org.goodev.droidddle.frag.team.TeamShotFragment;
import org.goodev.droidddle.frag.user.UserBucketFragment;
import org.goodev.droidddle.frag.user.UserFollowerFragment;
import org.goodev.droidddle.frag.user.UserFollowingFragment;
import org.goodev.droidddle.frag.user.UserLikedShotFragment;
import org.goodev.droidddle.frag.user.UserProjectFragment;
import org.goodev.droidddle.pojo.Team;
import org.goodev.droidddle.pojo.User;

import java.util.ArrayList;

/**
 * Created by goodev on 2015/1/14.
 */
public class TeamDetailsAdapter extends FragmentStatePagerAdapter {
    private static final int TYPE_SHOTS = 1;
    private static final int TYPE_PROJECTS = 2;
    private static final int TYPE_FOLLOWERS = 3;
    private static final int TYPE_FOLLOWINGS = 4;
    private static final int TYPE_BUCKETS = 5;
    private static final int TYPE_TEAMS = 6;
    private static final int TYPE_LIKES = 7;
    Team mShot;
    ArrayList<Integer> mItemsId;
    Context mContext;
    private int mScrollY;

    public TeamDetailsAdapter(Context context, FragmentManager fm, Team shot) {
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
        if (mShot.shotsCount > 0) {
            mItemsId.add(TYPE_SHOTS);
        }
        if (mShot.projectsCount > 0) {
            mItemsId.add(TYPE_PROJECTS);
        }
        if (mShot.followersCount > 0) {
            mItemsId.add(TYPE_FOLLOWERS);
        }
        if (mShot.followingsCount > 0) {
            mItemsId.add(TYPE_FOLLOWINGS);
        }

        if (mShot.bucketsCount > 0) {
            mItemsId.add(TYPE_BUCKETS);
        }
        if (mShot.membersCount > 0) {
            mItemsId.add(TYPE_TEAMS);
        }
        if (mShot.likesCount > 0) {
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
                count = mShot.shotsCount;
                break;
            case TYPE_FOLLOWERS:
                resId = R.string.followers_text;
                count = mShot.followersCount;
                break;
            case TYPE_FOLLOWINGS:
                resId = R.string.followings_text;
                count = mShot.followingsCount;
                break;
            case TYPE_LIKES:
                resId = R.string.likes_text;
                count = mShot.likesCount;
                break;
            case TYPE_BUCKETS:
                resId = R.string.buckets_text;
                count = mShot.bucketsCount;
                break;
            case TYPE_TEAMS:
                resId = R.string.members_text;
                count = mShot.membersCount;
                break;
            case TYPE_PROJECTS:
                resId = R.string.projects_text_with_number;
                count = mShot.projectsCount;
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
                return TeamShotFragment.newInstance(mShot);
            case TYPE_FOLLOWERS:
                return UserFollowerFragment.newInstance(new User(mShot));
            case TYPE_LIKES:
                return UserLikedShotFragment.newInstance(new User(mShot));
            case TYPE_BUCKETS:
                return UserBucketFragment.newInstance(new User(mShot));
            case TYPE_TEAMS:
                return TeamMemberFragment.newInstance(mShot);
            case TYPE_PROJECTS:
                return UserProjectFragment.newInstance(new User(mShot));
            case TYPE_FOLLOWINGS:
                return UserFollowingFragment.newInstance(new User(mShot));
        }

        return null;
    }


    @Override
    public int getCount() {
        return mItemsId.size();
    }
}
