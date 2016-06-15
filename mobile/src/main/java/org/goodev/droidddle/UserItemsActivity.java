package org.goodev.droidddle;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import org.goodev.droidddle.frag.team.TeamMemberFragment;
import org.goodev.droidddle.frag.team.TeamShotFragment;
import org.goodev.droidddle.frag.user.UserBucketFragment;
import org.goodev.droidddle.frag.user.UserFollowerFragment;
import org.goodev.droidddle.frag.user.UserFollowingFragment;
import org.goodev.droidddle.frag.user.UserLikedShotFragment;
import org.goodev.droidddle.frag.user.UserProjectFragment;
import org.goodev.droidddle.frag.user.UserShotFragment;
import org.goodev.droidddle.frag.user.UserTeamFragment;
import org.goodev.droidddle.pojo.Team;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.UiUtils;

import butterknife.ButterKnife;


public class UserItemsActivity extends UpActivity {
    public static final int TYPE_SHOT = 1;
    public static final int TYPE_PROJECT = 2;
    public static final int TYPE_FOLLOWER = 3;
    public static final int TYPE_FOLLOWING = 4;
    public static final int TYPE_BUCKET = 5;
    public static final int TYPE_TEAM = 6;
    public static final int TYPE_LIKE = 7;
    public static final int TYPE_MEMBER = 8;

    private int mType;
    private User mUser;
    private Team mTeam;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_user_items);
        ButterKnife.inject(this);
        Bundle extra = getIntent().getExtras();
        mType = extra.getInt(UiUtils.ARG_TYPE, TYPE_SHOT);
        if (extra.containsKey(UiUtils.ARG_USER)) {
            mUser = extra.getParcelable(UiUtils.ARG_USER);
        }
        if (extra.containsKey(UiUtils.ARG_TEAM)) {
            mTeam = extra.getParcelable(UiUtils.ARG_TEAM);
            mUser = new User(mTeam);
        }

        if (savedInstanceState == null) {
            Fragment fragment = getFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle();
    }

    private boolean isTeam() {
        return mTeam != null;
    }

    private Fragment getFragment() {
        Fragment fragment = null;
        switch (mType) {
            case TYPE_BUCKET:
                fragment = UserBucketFragment.newInstance(mUser);
                break;
            case TYPE_FOLLOWER:
                fragment = UserFollowerFragment.newInstance(mUser);
                break;
            case TYPE_FOLLOWING:
                fragment = UserFollowingFragment.newInstance(mUser);
                break;
            case TYPE_LIKE:
                fragment = UserLikedShotFragment.newInstance(mUser);
                break;
            case TYPE_PROJECT:
                fragment = UserProjectFragment.newInstance(mUser);
                break;
            case TYPE_SHOT:
                if (isTeam()) {
                    fragment = TeamShotFragment.newInstance(mTeam);
                } else {
                    fragment = UserShotFragment.newInstance(mUser);
                }
                break;
            case TYPE_TEAM:
                fragment = UserTeamFragment.newInstance(mUser);
                break;
            case TYPE_MEMBER:
                fragment = TeamMemberFragment.newInstance(mTeam);
                break;
            default:
                throw new IllegalArgumentException("type is error!");
        }
        return fragment;
    }

    private void setTitle() {
        int resId;
        switch (mType) {
            case TYPE_BUCKET:
                resId = R.string.title_user_bucket;
                break;
            case TYPE_FOLLOWER:
                resId = R.string.title_user_follower;
                break;
            case TYPE_FOLLOWING:
                resId = R.string.title_user_following;
                break;
            case TYPE_LIKE:
                resId = R.string.title_user_like;
                break;
            case TYPE_PROJECT:
                resId = R.string.title_user_project;
                break;
            case TYPE_SHOT:
                resId = R.string.title_user_shot;
                break;
            case TYPE_TEAM:
                resId = R.string.title_user_team;
                break;
            default:
                resId = R.string.title_user_bucket;
                break;
        }
        if (isTeam()) {
            setTitle(getString(resId, mTeam.name));
        } else {
            setTitle(getString(resId, mUser.name));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //        getMenuInflater().inflate(R.menu.menu_user_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.homeAsUp || id == R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
