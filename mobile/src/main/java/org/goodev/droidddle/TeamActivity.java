package org.goodev.droidddle;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.widget.Toast;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.frag.TeamFragment;
import org.goodev.droidddle.frag.team.TeamMemberFragment;
import org.goodev.droidddle.frag.team.TeamShotFragment;
import org.goodev.droidddle.frag.user.UserBucketFragment;
import org.goodev.droidddle.frag.user.UserFollowerFragment;
import org.goodev.droidddle.frag.user.UserFollowingFragment;
import org.goodev.droidddle.frag.user.UserLikedShotFragment;
import org.goodev.droidddle.frag.user.UserProjectFragment;
import org.goodev.droidddle.pojo.Team;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class TeamActivity extends DetailsActivity<Team> implements View.OnClickListener {

    private long mId;
    private Fragment mCurrentFragment;
    private UserFollowingFragment mFollowingsFragment;
    private UserBucketFragment mBucketsFragment;
    private UserProjectFragment mProjectsFragment;
    private TeamShotFragment mShotsFragment;
    private UserFollowerFragment mFollowersFragment;
    private UserLikedShotFragment mLikesFragment;
    private TeamMemberFragment mTeamsFragment;

    @Override
    protected int getLayoutRes() {
        return R.layout.user_layout;
    }

    @Override
    protected void initParams() {
        Bundle extra = getIntent().getExtras();
        long id = UiUtils.NO_ID;
        if (extra.containsKey(UiUtils.ARG_TEAM)) {
            mData = extra.getParcelable(UiUtils.ARG_TEAM);
            mId = mData.id;
        }
    }

    @Override
    protected void onPostMyCreate(Bundle savedInstanceState) {
        super.onPostMyCreate(savedInstanceState);
        if (savedInstanceState == null) {
            if (mData != null) {
                addUserFragment(mData);
            } else {
                finish();
            }
        }

    }

    @Override
    protected PagerAdapter getViewPagerAdapter() {
        return new TeamDetailsAdapter(this, getSupportFragmentManager(), mData);
    }


    private void getUser(long id) {
        if (!Utils.hasInternet(this)) {
            Toast.makeText(this, R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<Team> observable = ApiFactory.getService(this).getTeam(String.valueOf(id));
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((user) -> {
            updateUser(user);
        }, new ErrorCallback(this));
    }

    private void updateUser(Team user) {
        addUserFragment(user);
    }

    private void addUserFragment(Team user) {
        mData = user;
        if (!mHasTwoPane && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mData.name);
        }
        getSupportFragmentManager().beginTransaction().add(R.id.content_content, TeamFragment.newInstance(user)).commit();
    }

    @Override
    public void onClick(View v) {
        Activity activity = this;
        switch (v.getId()) {
            case R.id.user_location:
                UiUtils.launchMap(this, mData.location);
                break;
            case R.id.user_homepage:
                UiUtils.openUrl(activity, mData.links.web);
                break;
            case R.id.user_twitter:
                UiUtils.openUrl(activity, mData.links.twitter);
                break;
            case R.id.user_shots:
                openShotsTab();
                break;
            case R.id.user_projects:
                openProjectsTab();
                break;
            case R.id.user_followers:
                openFollowersTab();
                break;
            case R.id.user_followings:
                openFollowingsTab();
                break;
            case R.id.user_buckets:
                openBucketsTab();
                break;
            case R.id.user_team:
                openTeamsTab();
                break;
            case R.id.user_likes:
                openLikesTab();
                break;
        }
    }

    private void openFollowingsTab() {
        mToolbar.setSubtitle(getString(R.string.followings_text, mData.followingsCount));
        if (mFollowingsFragment == null) {
            mFollowingsFragment = UserFollowingFragment.newInstance(new User(mData));
        }
        if (mCurrentFragment != mFollowingsFragment) {
            mCurrentFragment = mFollowingsFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mFollowingsFragment).commit();
        }
        scrollToTop();
    }

    private void openBucketsTab() {
        mToolbar.setSubtitle(getString(R.string.buckets_text, mData.bucketsCount));
        if (mBucketsFragment == null) {
            mBucketsFragment = UserBucketFragment.newInstance(new User(mData));
        }
        if (mCurrentFragment != mBucketsFragment) {
            mCurrentFragment = mBucketsFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mBucketsFragment).commit();
        }
        scrollToTop();
    }

    private void openProjectsTab() {
        mToolbar.setSubtitle(getString(R.string.projects_text_with_number, mData.projectsCount));
        if (mProjectsFragment == null) {
            mProjectsFragment = UserProjectFragment.newInstance(new User(mData));
        }
        if (mCurrentFragment != mProjectsFragment) {
            mCurrentFragment = mProjectsFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mProjectsFragment).commit();
        }
        scrollToTop();
    }

    private void openShotsTab() {
        mToolbar.setSubtitle(getString(R.string.shots_text, mData.shotsCount));
        if (mShotsFragment == null) {
            mShotsFragment = TeamShotFragment.newInstance(mData);
        }
        if (mCurrentFragment != mShotsFragment) {
            mCurrentFragment = mShotsFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mShotsFragment).commit();
        }
        scrollToTop();
    }

    private void openFollowersTab() {
        mToolbar.setSubtitle(getString(R.string.followers_text, mData.followersCount));
        if (mFollowersFragment == null) {
            mFollowersFragment = UserFollowerFragment.newInstance(new User(mData));
        }
        if (mCurrentFragment != mFollowersFragment) {
            mCurrentFragment = mFollowersFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mFollowersFragment).commit();
        }
        scrollToTop();
    }

    private void openLikesTab() {
        mToolbar.setSubtitle(getString(R.string.likes_text, mData.likesCount));
        if (mLikesFragment == null) {
            mLikesFragment = UserLikedShotFragment.newInstance(new User(mData));
        }
        if (mCurrentFragment != mLikesFragment) {
            mCurrentFragment = mLikesFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mLikesFragment).commit();
        }
        scrollToTop();
    }

    private void openTeamsTab() {
        mToolbar.setSubtitle(getString(R.string.members_text, mData.membersCount));
        if (mTeamsFragment == null) {
            mTeamsFragment = TeamMemberFragment.newInstance(mData);
        }
        if (mCurrentFragment != mTeamsFragment) {
            mCurrentFragment = mTeamsFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mTeamsFragment).commit();
        }
        scrollToTop();
    }
}