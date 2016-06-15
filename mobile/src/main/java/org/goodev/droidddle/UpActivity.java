package org.goodev.droidddle;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.ct.CustomTabActivityHelper;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.Ads;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.ThemeUtil;
import org.goodev.droidddle.utils.UiUtils;

import butterknife.InjectView;
import butterknife.Optional;

import static org.goodev.droidddle.NavigationDrawerFragment.NAVDRAWER_ITEM_SETTINGS;

/**
 * Created by ADMIN on 2015/1/2.
 */
public abstract class UpActivity extends AppCompatActivity
        implements ThemeActivity, NavigationDrawerFragment.NavigationDrawerCallbacks {

    private int mTheme = -1;

    @Optional
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @SuppressWarnings("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.setTheme(this);
        setRequestedOrientation(Pref.getOrientation(this));
        UiUtils.initSystemBarTint(this);
        super.onCreate(savedInstanceState);
        onMyCreate(savedInstanceState);
        mCustomTabActivityHelper = new CustomTabActivityHelper();
        int color = ThemeUtil.getThemeColor(this, R.attr.colorPrimaryDark);
        UiUtils.setProfileCoverImage(mProfileCoverView, color, getResources());
        mDrawerLayout.setStatusBarBackgroundColor(color);
        initNavView();
        onPostMyCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layout) {
        super.setContentView(getBastLayout());
        NavigationView nv = (NavigationView) findViewById(R.id.navigation_view);
        View view = nv.inflateHeaderView(R.layout.drawer_header);
        mProfileCoverView = (ImageView) view.findViewById(R.id.profile_cover_image);
        mAvatarView = (SimpleDraweeView) view.findViewById(R.id.profile_image);
        mUserEmailView = (TextView) view.findViewById(R.id.profile_email_text);
        mUserNameView = (TextView) view.findViewById(R.id.profile_name_text);
        ViewGroup root = (ViewGroup) findViewById(R.id.main_content);
        getLayoutInflater().inflate(layout, root, true);
    }

    protected int getBastLayout() {
        return R.layout.activity_base_layout;
    }

    protected abstract void onMyCreate(Bundle savedInstanceState);

    protected void onPostMyCreate(Bundle savedInstanceState) {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

    }

    ;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
        } else {
            super.onBackPressed();
        }
    }

    public int getThemeId(int id) {
        return ThemeUtil.THEMES[id];
    }

    @Override
    public int getMyTheme() {
        return mTheme;
    }

    @Override
    public void setMyTheme(int id) {
        mTheme = id;
        setTheme(getThemeId(id));
    }

    @Override
    protected void onResume() {
        super.onResume();
//        StatService.onResume(this);
        ThemeUtil.reloadTheme(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        StatService.onPause(this);
    }

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @InjectView(R.id.navigation_view)
    NavigationView mNavigationView;

    //    @InjectView(R.id.profile_cover_image)
    ImageView mProfileCoverView;
    //    @InjectView(R.id.profile_image)
    SimpleDraweeView mAvatarView;

    //    @InjectView(R.id.profile_email_text)
    TextView mUserEmailView;

    //    @InjectView(R.id.profile_name_text)
    TextView mUserNameView;

//    @InjectView(R.id.profile_shots_text)
//    TextView mShotsView;
//
//    @InjectView(R.id.profile_followings_text)
//    TextView mFollowingsView;
//
//    @InjectView(R.id.profile_followers_text)
//    TextView mFollowersView;

    protected void initNavView() {
        MenuItem item = mNavigationView.getMenu().findItem(R.id.drawer_home);
        item.setChecked(false);
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        onNavigationDrawerItemSelected(menuItem.getItemId());
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

        updateData(App.getUser());
    }

    @Override
    public void onNavigationDrawerItemSelected(int id) {
        if (getSelfNavDrawerItem() == id) {
            return;
        }
        if (id == NAVDRAWER_ITEM_SETTINGS) {
            UiUtils.openSettings(this);
            return;
        } else if (id == R.id.drawer_about) {
            UiUtils.openAbout(this);
            return;
        } else if (id == R.id.drawer_rewards) {
            Ads.openServey(this);
            return;
        } else if (id == R.id.drawer_dl) {
            UiUtils.openDownloadedImages(this);
            return;
        } else {
            UiUtils.openMain(this, id);
        }

    }

    private int mCurrentDrawerId = -1;

    protected int getSelfNavDrawerItem() {
        return mCurrentDrawerId;
    }

    protected void setNavDrawerItem(int id) {
        mCurrentDrawerId = id;
        mNavigationView.getMenu().findItem(id).setChecked(true);
    }

    private void updateData(User u) {
        if (u == null) {
            mUserEmailView.setText("");
            return;
        }
        UpActivity activity = this;
        Pref.setOAuthUserId(activity, u.id);
        mAvatarView.setImageURI(Uri.parse(u.avatarUrl));
        //        Glide.with(getActivity()).load(u.avatarUrl).placeholder(R.drawable.person_image_empty).into(mAvatarView);
        mUserNameView.setText(u.name);
        mUserEmailView.setText(u.username);
        String format = UiUtils.getFormat(u.followersCount, u.shotsCount, u.followingsCount);
//        mFollowersView.setText(
//                getString(R.string.prefile_followers, String.format(format, u.followersCount)));
//        mFollowingsView.setText(
//                getString(R.string.prefile_followings, String.format(format, u.followingsCount)));
//        mShotsView.setText(getString(R.string.prefile_shots, String.format(format, u.shotsCount)));

        //        mAvatarView.setImageDrawable(UiUtils.getSvgDrawable(getActivity(),R.raw.ic_drawer_dribbble));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(this);
    }

    protected CustomTabActivityHelper mCustomTabActivityHelper;
}
