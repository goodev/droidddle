package org.goodev.droidddle;

import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.frag.HomeFragment;
import org.goodev.droidddle.frag.user.BaseUserFragment;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.Ads;
import org.goodev.droidddle.utils.AppRater;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.ThemeUtil;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.OnFilterListener;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static org.goodev.droidddle.NavigationDrawerFragment.NAVDRAWER_ITEM_MY_BUCKETS;


public class MainActivity extends BaseActivity implements ThemeActivity {

    public static final long FIVE = 5000;
    long mLastBackTime;
    @InjectView(R.id.fab)
    FloatingActionButton mFab;
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.filter_layout)
    View mFilterLayout;
    @InjectView(R.id.shots_sort)
    TextView mSpinnerSort;
    @InjectView(R.id.shots_list)
    TextView mSpinnerList;
    @InjectView(R.id.shots_timeframe)
    TextView mSpinnerTimeframe;
    @InjectView(R.id.filter_padding1)
    View mFilterPadding1;
    String[] mSortValues;
    String[] mSortEntries;
    String[] mListValues;
    String[] mListEntries;
    String[] mTimeframeValues;
    String[] mTimeEntries;


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int mTheme = -1;
    private boolean mIsCleanerMode = true;

    private Handler mHandler;
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.setTheme(this);
        boolean haveToken = OAuthUtils.haveToken(this);
        final String action = getIntent().getAction();
        if (UiUtils.ACTION_OPEN_FOLLOWING.equals(action)) {
            mOpenFollowing = true;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        NavigationView nv = (NavigationView) findViewById(R.id.navigation_view);
        View view = nv.inflateHeaderView(R.layout.drawer_header);
        view.findViewById(R.id.chosen_account_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAccountClicked(v);
            }
        });
        mProfileCoverView = (ImageView) view.findViewById(R.id.profile_cover_image);
        mAvatarView = (SimpleDraweeView) view.findViewById(R.id.profile_image);
        mUserEmailView = (TextView) view.findViewById(R.id.profile_email_text);
        mUserNameView = (TextView) view.findViewById(R.id.profile_name_text);

        int color = ThemeUtil.getThemeColor(this, R.attr.colorPrimaryDark);
        mDrawerLayout.setStatusBarBackgroundColor(color);
        mIsCleanerMode = Pref.isCleanerMode(this);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        setupFab();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(UiUtils.EXTRA_MENU_ID)) {
            int id = extras
                    .getInt(UiUtils.EXTRA_MENU_ID, NavigationDrawerFragment.NAVDRAWER_ITEM_HOME);
            setNavDrawerItem(id);
        } else if (mOpenFollowing && savedInstanceState == null) {
            setNavDrawerItem(NavigationDrawerFragment.NAVDRAWER_ITEM_MY_FOLLOWING);
        } else {
            setNavDrawerItem(getSelfNavDrawerItem());
        }
        updateFragmentTitle(getSelfNavDrawerItem());
        initNavView();
        mOAuthedUser = App.getUser();
        if (haveToken) {
            try {
                parseData();
            } catch (Exception e) {
                // maybe have exception when parse url
                e.printStackTrace();
            }
        }
        if (mOAuthedUser != null) {
            updateData(mOAuthedUser);
        }
        if (savedInstanceState == null) {
            addMainFragment();
            //            onUpdateFragment(mHomeFragment);
        } else {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_content);
            onUpdateFragment(f);
        }

        refreshOAuthToken();
        setFabStatus(getSelfNavDrawerItem());

        if (UiUtils.ACTION_OPEN_SHOT.equals(action)) {
            Shot shot = getIntent().getParcelableExtra(UiUtils.ARG_SHOT);
            if (shot != null) {
                UiUtils.launchShot(this, shot);
            }
        }

        setupTitleClick();

        new AppRater(this)
                .setAppTitle(getString(R.string.app_name))
                .init();

        setupShotFilter();

        Ads.checkUpdate(this);
    }

    private void setupShotFilter() {
        Resources res = getResources();
        mSortValues = res.getStringArray(R.array.shots_sort_value);
        mListValues = res.getStringArray(R.array.shots_list_value);
        mTimeframeValues = res.getStringArray(R.array.shots_timeframe_value);
        if (mIsCleanerMode) {
            mSpinnerTimeframe.setVisibility(View.GONE);
            mFilterPadding1.setVisibility(View.GONE);
        } else {
            mSpinnerTimeframe.setVisibility(View.VISIBLE);
            mFilterPadding1.setVisibility(View.VISIBLE);
        }

//        ViewCompat.setBackgroundTintMode(mSpinnerList, PorterDuff.Mode.SRC_IN);
//        ViewCompat.setBackgroundTintList(mSpinnerList, ColorStateList.valueOf(Color.WHITE));
        mSortEntries = res.getStringArray(R.array.shots_sort_entry);
        mSpinnerSort.setText(mSortEntries[0]);
        mListEntries = res.getStringArray(R.array.shots_list_entry);
        mSpinnerList.setText(mListEntries[0]);
        mTimeEntries = res.getStringArray(R.array.shots_timeframe_entry);
        mSpinnerTimeframe.setText(mTimeEntries[0]);

        if (!TextUtils.isEmpty(mSort)) {
            mSpinnerSort.setText(mSortEntries[getSelection(mSortValues, mSort)]);
        }
        if (!TextUtils.isEmpty(mList)) {
            mSpinnerList.setText(mListEntries[getSelection(mListValues, mList)]);
        }
        if (!TextUtils.isEmpty(mTimeframe)) {
            mSpinnerTimeframe.setText(mTimeEntries[getSelection(mTimeframeValues, mTimeframe)]);
        }

        UiUtils.setupFilterPopupMenu(this, mSpinnerTimeframe, new OnFilterListener() {
            @Override
            public void update(int pos) {
                if (mCurrentFragment instanceof HomeFragment) {
                    mSpinnerTimeframe.setText(mTimeEntries[pos]);
                    ((HomeFragment) mCurrentFragment).onTimeframeItemSelected(mTimeframeValues[pos]);
                }
            }
        }, mTimeEntries);
        UiUtils.setupFilterPopupMenu(this, mSpinnerSort, new OnFilterListener() {
            @Override
            public void update(int pos) {
                if (mCurrentFragment instanceof HomeFragment) {
                    mSpinnerSort.setText(mSortEntries[pos]);
                    ((HomeFragment) mCurrentFragment).onSortItemSelected(mSortValues[pos]);
                }
            }
        }, mSortEntries);
        UiUtils.setupFilterPopupMenu(this, mSpinnerList, new OnFilterListener() {
            @Override
            public void update(int pos) {
                if (mCurrentFragment instanceof HomeFragment) {
                    mSpinnerList.setText(mListEntries[pos]);
                    ((HomeFragment) mCurrentFragment).onListItemSelected(mListValues[pos]);
                }
            }
        }, mListEntries);
    }

    private int getSelection(String[] values, String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    private void setupTitleClick() {
        try {
            Field title = Toolbar.class.getDeclaredField("mTitleTextView");
            title.setAccessible(true);
            TextView tv = (TextView) title.get(mToolbar);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCurrentFragment instanceof HomeFragment) {
                        ((HomeFragment) mCurrentFragment).scrollToTop();
                    } else if (mCurrentFragment instanceof BaseUserFragment) {
                        ((BaseUserFragment) mCurrentFragment).scrollToTop();
                    }
                }
            });
            ViewGroup.LayoutParams lp = tv.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    void onClick(View v) {
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mOAuthedUser != null) {
            checkIfOpenFollowing();
        }
    }

    private final static String KEY = "fe6aff28-9150-4037-982e-96ef9560dd87";
    //    private void initPollFish() {
    //        PollFish.init(this, KEY, Position.BOTTOM_RIGHT, 0, new PollfishSurveyReceivedListener() {
    //            @Override
    //            public void onPollfishSurveyReceived(boolean playfulSurveys, int surveyPrice) {
    //                Log.e("Pollfish", "Pollfish survey received - Playful survey: " + playfulSurveys + " with price: " + surveyPrice);
    //            }
    //        }, null, null , null, null , null);
    //    }


    private boolean mOpenFollowing;

    private void checkIfOpenFollowing() {
        if (mOpenFollowing) {
            mOpenFollowing = false;
            setNavDrawerItem(NavigationDrawerFragment.NAVDRAWER_ITEM_HOME);
            onNavigationDrawerItemSelected(NavigationDrawerFragment.NAVDRAWER_ITEM_MY_FOLLOWING);

            mDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateTitle();
                }
            }, 200);
        }
    }

    private void parseData() {
        String data = getIntent().getDataString();
        if (TextUtils.isEmpty(data)) {
            return;
        }

        Uri uri = Uri.parse(data);
        String path = uri.getPath();
        if (UiUtils.PATH_SHOTS.equals(path)) {
            mList = uri.getQueryParameter(UiUtils.PARAM_LIST);
            mSort = uri.getQueryParameter(UiUtils.PARAM_SORT);
            mTimeframe = uri.getQueryParameter(UiUtils.PARAM_TIMEFRAME);

        } else {
            UiUtils.openDroidddleLinks(this, data);
        }
    }

    private void setupFab() {
        if (UiUtils.hasLollipop()) {
//            mFab.setColorRippleResId(R.color.ripple_material_light);
//            mFab.setColorPressed(ThemeUtil.getThemeColor(this, R.attr.navdrawerTintColor));
        } else {
//            mFab.setColorPressed(ThemeUtil.getThemeColor(this, R.attr.colorAccent));
        }
    }

    @OnClick(R.id.fab)
    void onFabClick(View view) {
        UiUtils.launchCreateActivity(this, UiUtils.TYPE_SHOT);

    }

    @OnLongClick(R.id.fab)
    boolean onFabLongClick(View view) {
        UiUtils.launchCreateActivity(this, UiUtils.TYPE_BUCKET);
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public void setOAuthUser(User u) {
        boolean first = mOAuthedUser == null;
        super.setOAuthUser(u);
//        if (first) {
//            startService(BucketWatchFaceLoader.getServiceIntent(this));
//        }
        if (first && mHomeFragment != null) {
            mHomeFragment.getShots();
            //            addMainFragment();
        }

        checkIfOpenFollowing();
    }

    @Override
    public void onBackPressed() {
        Ads.onBackKey(this);
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return;
        }
        long now = System.currentTimeMillis();
        if (now - mLastBackTime < FIVE) {
            super.onBackPressed();
        } else {
            mLastBackTime = now;
            UiUtils.showToast(this, R.string.back_again_to_exit);
            //            testSearch();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.search_by_text) {
            UiUtils.launchSearch(this);
            return true;
        } else if (id == R.id.search_by_color) {
            UiUtils.launchColorSearchActivity(this);
            return true;
        }
        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int id) {
        super.onNavigationDrawerItemSelected(id);
        setFabStatus(id);

    }

    @Override
    public void onUpdateFragment(Fragment f) {
        mCurrentFragment = f;
        if (f instanceof HomeFragment) {
            mHomeFragment = (HomeFragment) f;
            mFilterLayout.setVisibility(View.VISIBLE);
//            ViewCompat.setElevation(mToolbar, 0);
//            ViewCompat.setTranslationZ(mToolbar, 0);
        } else {
            mFilterLayout.setVisibility(View.GONE);
//            ViewCompat.setElevation(mToolbar, mElevation);
//            ViewCompat.setTranslationZ(mToolbar, mElevation);
        }
    }

    private void setFabStatus(int id) {
        if (id == NAVDRAWER_ITEM_MY_BUCKETS) {
            mFab.setVisibility(View.GONE);
        } else {
            mFab.setVisibility(View.VISIBLE);
        }
    }


    void testSearch() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String s = ApiFactory.search(getApplication(), "ui", 1);
                System.out.println(s);
                return s;
            }
        }.execute();
    }

    @Override
    public int getMyTheme() {
        return mTheme;
    }

    @Override
    public void setMyTheme(int id) {
        mTheme = id;
        setTheme(ThemeUtil.THEMES[id]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean cleanerMode = Pref.isCleanerMode(this);
        if (mIsCleanerMode != cleanerMode) {
            recreate();
        }
        ThemeUtil.reloadTheme(this);
    }

}
