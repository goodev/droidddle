package org.goodev.droidddle;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.ct.CustomTabActivityHelper;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.frag.HomeFragment;
import org.goodev.droidddle.frag.user.UserBucketFragment;
import org.goodev.droidddle.frag.user.UserFollowingShotFragment;
import org.goodev.droidddle.frag.user.UserLikedShotFragment;
import org.goodev.droidddle.frag.user.UserProjectFragment;
import org.goodev.droidddle.frag.user.UserShotFragment;
import org.goodev.droidddle.frag.user.UserTeamFragment;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.Ads;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.ThemeUtil;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;

import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.goodev.droidddle.NavigationDrawerFragment.NAVDRAWER_ITEM_HOME;
import static org.goodev.droidddle.NavigationDrawerFragment.NAVDRAWER_ITEM_MY_BUCKETS;
import static org.goodev.droidddle.NavigationDrawerFragment.NAVDRAWER_ITEM_MY_FOLLOWING;
import static org.goodev.droidddle.NavigationDrawerFragment.NAVDRAWER_ITEM_MY_LIKES;
import static org.goodev.droidddle.NavigationDrawerFragment.NAVDRAWER_ITEM_MY_PROJECTS;
import static org.goodev.droidddle.NavigationDrawerFragment.NAVDRAWER_ITEM_MY_SHOTS;
import static org.goodev.droidddle.NavigationDrawerFragment.NAVDRAWER_ITEM_MY_TEAMS;
import static org.goodev.droidddle.NavigationDrawerFragment.NAVDRAWER_ITEM_SETTINGS;

/**
 * Created by goodev on 2014/12/15.
 */
public abstract class BaseActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String FRAG_TAG = "f_tag_";
    protected User mOAuthedUser;
    protected HomeFragment mHomeFragment;
    String mTimeframe;
    String mList;
    String mSort;
    @InjectView(R.id.navigation_view)
    NavigationView mNavigationView;

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    ImageView mProfileCoverView;


    SimpleDraweeView mAvatarView;
    TextView mUserEmailView;
    TextView mUserNameView;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int mCurrentDrawerId = NAVDRAWER_ITEM_HOME;
    private boolean mPaused;
    protected float mElevation;

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * of BaseActivity override this to indicate what nav drawer item corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected int getSelfNavDrawerItem() {
        return mCurrentDrawerId;
    }

    protected void setNavDrawerItem(int id) {
        mCurrentDrawerId = id;
        mNavigationView.getMenu().findItem(id).setChecked(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(Pref.getOrientation(this));
        UiUtils.initSystemBarTint(this);
        super.onCreate(savedInstanceState);
        mElevation = getResources().getDimension(R.dimen.elevation);
        if (savedInstanceState != null) {
            mCurrentDrawerId = savedInstanceState.getInt(UiUtils.STATE_SELECTED_POSITION);
        }

        mCustomTabActivityHelper = new CustomTabActivityHelper();
    }

    ActionBarDrawerToggle mDrawerToggle;

    protected void initNavView() {
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                onNavigationDrawerItemSelected(menuItem.getItemId());
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this,                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                updateTitle(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };


        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        int color = ThemeUtil.getThemeColor(this, R.attr.colorPrimaryDark);
        UiUtils.setProfileCoverImage(mProfileCoverView, color, getResources());

        Intent intent = getIntent();

        if (intent.hasExtra(UiUtils.KEY_USER)) {
            mOAuthedUser = intent.getParcelableExtra(UiUtils.KEY_USER);
        }
        UiUtils.checkPackageName(this);
        mDrawerToggle.syncState();
        mDrawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTitle();
            }
        }, 200);

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(UiUtils.STATE_SELECTED_POSITION, mCurrentDrawerId);
        mPaused = true;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPaused = false;
    }

    public void addMainFragment() {
        final int id = mOAuthedUser == null ? NAVDRAWER_ITEM_HOME : mCurrentDrawerId;
        final Fragment fragment = getFragment(id);
        updateFragmentTitle(id);
        if (fragment instanceof HomeFragment) {
            mHomeFragment = (HomeFragment) fragment;
        }
        // update the main content by replacing fragments
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.main_content, fragment).commit();
        onUpdateFragment(fragment);

    }

    @Override
    public void onNavigationDrawerItemSelected(int id) {
        if (id == NAVDRAWER_ITEM_SETTINGS) {
            UiUtils.openSettings(this);
            return;
        } else if (id == R.id.drawer_about) {
            UiUtils.openAbout(this);
            return;
        } else if (id == R.id.drawer_dl) {
            UiUtils.openDownloadedImages(this);
            return;
        } else if (id == R.id.drawer_rewards) {
            Ads.openServey(this);
            return;
        }
        if (getSelfNavDrawerItem() == id) {
            return;
        }
        if (mOAuthedUser == null) {
            UiUtils.showToast(this, R.string.please_login_first);
            return;
        }
        mCurrentDrawerId = id;
        setNavDrawerItem(id);
        updateFragmentTitle(id);
        Fragment fragment = getFragment(id);
        if (fragment instanceof HomeFragment) {
            mHomeFragment = (HomeFragment) fragment;
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        //TODO bug: https://code.google.com/p/android/issues/detail?id=78701
        Fragment f = fragmentManager.findFragmentById(R.id.main_content);
        if (f != null) {
            ft.remove(f);
        }
        ft.replace(R.id.main_content, fragment, FRAG_TAG).commit();

        onUpdateFragment(fragment);
    }

    public void onUpdateFragment(Fragment f) {
    }

    public Fragment getFragment(int id) {
        Fragment fragment = null;
        switch (id) {
            case NAVDRAWER_ITEM_HOME:
                fragment = HomeFragment.newInstance(mList, mSort, mTimeframe);
                break;
            case NAVDRAWER_ITEM_MY_FOLLOWING:
                fragment = UserFollowingShotFragment.newInstance(mOAuthedUser);
                break;
            case NAVDRAWER_ITEM_MY_SHOTS:
                fragment = UserShotFragment.newInstance(mOAuthedUser);
                break;
            case NAVDRAWER_ITEM_MY_BUCKETS:
                fragment = UserBucketFragment.newInstance(mOAuthedUser);
                break;
            case NAVDRAWER_ITEM_MY_PROJECTS:
                fragment = UserProjectFragment.newInstance(mOAuthedUser);
                break;
            case NAVDRAWER_ITEM_MY_TEAMS:
                fragment = UserTeamFragment.newInstance(mOAuthedUser);
                break;
            case NAVDRAWER_ITEM_MY_LIKES:
                fragment = UserLikedShotFragment.newInstance(mOAuthedUser);
                break;
        }
        return fragment;
    }

    public void updateFragmentTitle(int id) {
        switch (id) {
            case NAVDRAWER_ITEM_HOME:
                updateTitle(R.string.app_name);
                break;
            case NAVDRAWER_ITEM_MY_FOLLOWING:
                updateTitle(R.string.title_my_following_shots);
                break;
            case NAVDRAWER_ITEM_MY_SHOTS:
                updateTitle(R.string.title_my_shots);
                break;
            case NAVDRAWER_ITEM_MY_BUCKETS:
                updateTitle(R.string.title_my_buckets);
                break;
            case NAVDRAWER_ITEM_MY_PROJECTS:
                updateTitle(R.string.title_my_projects);
                break;
            case NAVDRAWER_ITEM_MY_TEAMS:
                updateTitle(R.string.title_my_teams);
                break;
            case NAVDRAWER_ITEM_MY_LIKES:
                updateTitle(R.string.title_my_likes);
                break;
        }
    }

    private void updateTitle(int res) {
        mTitle = getString(res);
    }

    public void updateTitle() {
        if (mTitle == null) {
            mTitle = getString(R.string.app_name);
        }
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mDrawerLayout.isDrawerOpen(mNavigationView)) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    //    @OnClick(R.id.chosen_account_view)
    public void onAccountClicked(View view) {
        if (OAuthUtils.haveToken(this) && mOAuthedUser != null) {
            UiUtils.launchUser(this, mOAuthedUser);
        } else {
            startOAuth();
        }
    }

    //if not authed start oauth , else get access token
    public void refreshOAuthToken() {
        startOAuth();
    }

    private void startOAuth() {
        String token = OAuthUtils.getAccessToken(this);
        if (OAuthUtils.ACCESS_TOKEN.equals(token)) {
            OAuthUtils.startOauth(this);
        }
    }

    public void updateUser(String accessToken) {
        if (mOAuthedUser != null) {
            return;
        }
        try {
            if (!Utils.hasInternet(this)) {
                Toast.makeText(this, R.string.check_network, Toast.LENGTH_SHORT).show();
                return;
            }
            ApiFactory.resetApiService();
            Observable<User> user = ApiFactory.getService(this, accessToken)
                    .getOAuthUser();
            user.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe((u) -> {
                        updateData(u);
                    }, new ErrorCallback(this))
            ;
        } catch (Exception e) {
            //TODO have NEP...
            e.printStackTrace();
        }
    }

    protected void updateData(User u) {
        BaseActivity activity = (BaseActivity) this;
        setOAuthUser(u);
        Pref.setOAuthUserId(activity, u.id);
        //TODO get user data when app opened, use this cache data until exit app.

        mAvatarView.setImageURI(Uri.parse(u.avatarUrl));
        mUserNameView.setText(u.name);
        mUserEmailView.setText(u.username);
        String format = UiUtils.getFormat(u.followersCount, u.shotsCount, u.followingsCount);
//        mFollowersView.setText(getString(R.string.prefile_followers, String.format(format, u.followersCount)));
//        mFollowingsView.setText(getString(R.string.prefile_followings, String.format(format, u.followingsCount)));
//        mShotsView.setText(getString(R.string.prefile_shots, String.format(format, u.shotsCount)));
        //        mAvatarView.setImageDrawable(UiUtils.getSvgDrawable(getActivity(),R.raw.ic_drawer_dribbble));
    }

    public void setOAuthUser(User u) {
        mOAuthedUser = u;
        App.setOAuthUser(u);
    }

    public void restoreTitle() {
        getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String token = OAuthUtils.getAccessToken(this);
        if (!OAuthUtils.ACCESS_TOKEN.equals(token)) {
            updateUser(token);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OAuthUtils.LOGIN_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.login_canceled, Toast.LENGTH_SHORT).show();
                mDrawerLayout.openDrawer(mNavigationView);
            }
        }
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
