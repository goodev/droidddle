package org.goodev.droidddle;


import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.ThemeUtil;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    // symbols for navdrawer items (indices must correspond to array below). This is
    // not a list of items that are necessarily *present* in the Nav Drawer; rather,
    // it's a list of all possible items.
    protected static final int NAVDRAWER_ITEM_HOME = R.id.drawer_home;

    protected static final int NAVDRAWER_ITEM_MY_FOLLOWING = R.id.drawer_following;

    protected static final int NAVDRAWER_ITEM_MY_SHOTS = R.id.drawer_shots;

    protected static final int NAVDRAWER_ITEM_MY_BUCKETS = R.id.drawer_buckets;

    protected static final int NAVDRAWER_ITEM_MY_PROJECTS = R.id.drawer_projects;

    protected static final int NAVDRAWER_ITEM_MY_TEAMS = R.id.drawer_team;

    protected static final int NAVDRAWER_ITEM_MY_LIKES = R.id.drawer_likes;

    protected static final int NAVDRAWER_ITEM_SETTINGS = R.id.drawer_settings;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;
    protected static final int NAVDRAWER_ITEM_PROFILE_TITLE = -4;
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    // delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;
    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{R.string.navdrawer_item_home, //
            R.string.navdrawer_item_following, //
            R.string.navdrawer_item_shots, //
            R.string.navdrawer_item_buckets, //
            R.string.navdrawer_item_projects, //
            R.string.navdrawer_item_teams, //
            R.string.navdrawer_item_likes, //
            R.string.navdrawer_item_settings //
    };
    // icons for navdrawer items (indices must correspond to above array)
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[]{R.drawable.ic_drawer_dribbble,  //
            R.drawable.ic_drawer_following,  //
            R.drawable.ic_drawer_shots,  //
            R.drawable.ic_drawer_buckets,  //
            R.drawable.ic_drawer_projects,  //
            R.drawable.ic_drawer_team,  //
            R.drawable.ic_drawer_favorite,  //
            R.drawable.ic_drawer_settings,  //
    };
    @InjectView(R.id.chosen_account_view)
    View mAccountView;
    @InjectView(R.id.profile_image)
    SimpleDraweeView mAvatarView;
    @InjectView(R.id.profile_email_text)
    TextView mUserEmailView;
    @InjectView(R.id.profile_name_text)
    TextView mUserNameView;
    @InjectView(R.id.profile_shots_text)
    TextView mShotsView;
    @InjectView(R.id.profile_followings_text)
    TextView mFollowingsView;
    @InjectView(R.id.profile_followers_text)
    TextView mFollowersView;
    @InjectView(R.id.navdrawer_items_list)
    ViewGroup mDrawerItemsListContainer;
    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;
    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mDrawerListView;
    private View mFragmentContainerView;
    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    // list of navdrawer items that were actually added to the navdrawer, in order
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();

    // views that correspond to each navdrawer item, null if not yet created
    private View[] mNavDrawerItemViews = null;
    private Handler mHandler;
    private User mUser;
    private int mDrawerTextColor = -1;
    private int mDrawerIconTint = -1;
    private int mTintColor = -1;

    public NavigationDrawerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDrawerTextColor = ThemeUtil.getThemeColor(getActivity(), R.attr.navdrawerTextColor);
        mDrawerIconTint = ThemeUtil.getThemeColor(getActivity(), R.attr.navdrawerIconTint);
        mTintColor = ThemeUtil.getThemeColor(getActivity(), R.attr.navdrawerTintColor);
        mHandler = new Handler();
        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        //        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerListView = inflater.inflate(R.layout.navdrawer, container, false);
        ButterKnife.inject(this, mDrawerListView);
        // populate the nav drawer with the correct items
        populateNavDrawer();
        if (savedInstanceState != null) {
            User user = savedInstanceState.getParcelable(UiUtils.ARG_USER);
            if (user != null) {
                updateData(user);
            } else if (OAuthUtils.haveToken(getActivity())) {
                updateUser();
            }
        } else if (OAuthUtils.haveToken(getActivity())) {
            updateUser();
        }
        return mDrawerListView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
        outState.putParcelable(UiUtils.ARG_USER, mUser);
    }

    /**
     * Populates the navigation drawer with the appropriate items.
     */
    private void populateNavDrawer() {
        mNavDrawerItems.clear();
        mNavDrawerItems.add(NAVDRAWER_ITEM_HOME);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR);
        //        mNavDrawerItems.add(NAVDRAWER_ITEM_PROFILE_TITLE);
        // Explore is always shown
        mNavDrawerItems.add(NAVDRAWER_ITEM_MY_FOLLOWING);
        mNavDrawerItems.add(NAVDRAWER_ITEM_MY_SHOTS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_MY_BUCKETS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_MY_PROJECTS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_MY_TEAMS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_MY_LIKES);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR_SPECIAL);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SETTINGS);

        createNavDrawerItems();
    }

    private void createNavDrawerItems() {
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = getSelfNavDrawerItem() == itemId;
        int layoutToInflate = 0;
        if (itemId == NAVDRAWER_ITEM_SEPARATOR) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else if (itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else if (itemId == NAVDRAWER_ITEM_PROFILE_TITLE) {
            layoutToInflate = R.layout.navdrawer_profile;
        } else {
            layoutToInflate = R.layout.navdrawer_item;
        }
        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (isSeparator(itemId)) {
            // we are done
            UiUtils.setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = itemId >= 0 && itemId < NAVDRAWER_ICON_RES_ID.length ? NAVDRAWER_ICON_RES_ID[itemId] : 0;
        int titleId = itemId >= 0 && itemId < NAVDRAWER_TITLE_RES_ID.length ? NAVDRAWER_TITLE_RES_ID[itemId] : 0;

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(getString(titleId));

        formatNavDrawerItem(view, itemId, selected);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });

        return view;
    }

    private void onNavDrawerItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (isSettings(itemId)) {
            UiUtils.openSettings(getActivity());
        } else if (isSpecialItem(itemId)) {
            goToNavDrawerItem(itemId);
        } else {
            // launch the target Activity after a short delay, to allow the close animation to play
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNavDrawerItem(itemId);
                }
            }, NAVDRAWER_LAUNCH_DELAY);

            // change the active item on the list so the user can see the item changed
            setSelectedNavDrawerItem(itemId);
            //TODO fade out the main content
            //            View mainContent = findViewById(R.id.main_content);
            //            if (mainContent != null) {
            //                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            //            }
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private View findViewById(int id) {
        return getActivity().findViewById(id);
    }

    private void goToNavDrawerItem(int itemId) {
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(itemId);
        }
    }

    private LayoutInflater getLayoutInflater() {
        return getActivity().getLayoutInflater();
    }

    private boolean isSpecialItem(int itemId) {
        return itemId == NAVDRAWER_ITEM_SETTINGS;
    }

    private boolean isSettings(int itemId) {
        return itemId == NAVDRAWER_ITEM_SETTINGS;
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL || itemId == NAVDRAWER_ITEM_PROFILE_TITLE;
    }

    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (isSeparator(itemId)) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ? mTintColor : mDrawerTextColor);
        iconView.setColorFilter(selected ? mTintColor : mDrawerIconTint);
    }

    /**
     * Sets up the given navdrawer item's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisItemId, itemId == thisItemId);
                }
            }
        }
    }

    private void updateData(User u) {
        BaseActivity activity = (BaseActivity) getActivity();
        activity.setOAuthUser(u);
        mUser = u;
        Pref.setOAuthUserId(activity, u.id);
        //TODO get user data when app opened, use this cache data until exit app.

        mAvatarView.setImageURI(Uri.parse(u.avatarUrl));
//        Glide.with(getActivity()).load(u.avatarUrl).placeholder(R.drawable.person_image_empty).into(mAvatarView);
        mUserNameView.setText(u.name);
        mUserEmailView.setText(u.username);
        String format = UiUtils.getFormat(u.followersCount, u.shotsCount, u.followingsCount);
        mFollowersView.setText(getString(R.string.prefile_followers, String.format(format, u.followersCount)));
        mFollowingsView.setText(getString(R.string.prefile_followings, String.format(format, u.followingsCount)));
        mShotsView.setText(getString(R.string.prefile_shots, String.format(format, u.shotsCount)));

        //        mAvatarView.setImageDrawable(UiUtils.getSvgDrawable(getActivity(),R.raw.ic_drawer_dribbble));
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    private int getSelfNavDrawerItem() {
        BaseActivity activity = (BaseActivity) getActivity();
        // What nav drawer item should be selected?
        int selfItem = activity.getSelfNavDrawerItem();
        return selfItem;
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        BaseActivity activity = (BaseActivity) getActivity();
        // What nav drawer item should be selected?
        int selfItem = activity.getSelfNavDrawerItem();

        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                ((BaseActivity) getActivity()).updateTitle(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                ((BaseActivity) getActivity()).restoreTitle();
                //                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            //            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            //            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @OnClick(R.id.chosen_account_view)
    public void onAccountClicked(View view) {
        if (OAuthUtils.haveToken(getActivity()) && mUser != null) {
            UiUtils.launchUser(getActivity(), mUser);
        } else {
            startOAuth();
        }
    }

    private void startOAuth() {
//        OAuthManager.OAuthCallback<Credential> callback1 = (future) -> {
//            try {
//                Credential credential = future.getResult();
//                OAuthUtils.saveAccessToken(getActivity(), credential.getAccessToken());
//                updateUser(credential.getAccessToken());
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (CancellationException e) {
//                OAuthUtils.oauthCanceled(getActivity());
//                //TODO user cancelled
//                e.printStackTrace();
//            }
//        };
//        OAuthUtils.startOauth((ActionBarActivity) getActivity(), callback1, true);
    }

    public void updateUser(String accessToken) {
        if (mUser != null) {
            return;
        }
        try {
            if (!Utils.hasInternet(getActivity())) {
                return;
            }
            Observable<User> user = ApiFactory.getService(getActivity(), accessToken)
                    .getOAuthUser();
            user.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe((u) -> {
                        updateData(u);
                    }, new ErrorCallback(getActivity()));
        } catch (Exception e) {
            //TODO have NEP...
            e.printStackTrace();
        }
    }

    private void updateUser() {
        Observable<User> user = ApiFactory.getService(getActivity()).getOAuthUser();
        user.observeOn(AndroidSchedulers.mainThread()).subscribe((u) -> {
            updateData(u);
        }, new ErrorCallback(getActivity()));
    }


    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int id);
    }
}
