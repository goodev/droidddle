package org.goodev.droidddle.frag;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ApiService;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.Scene;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.ProgressView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends StatFragment implements Toolbar.OnMenuItemClickListener {
    @Optional
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    boolean mHasTwoPane;

    @InjectView(R.id.user_image)
    SimpleDraweeView mUserImageView;
    @InjectView(R.id.user_name)
    TextView mUserNameView;
    @InjectView(R.id.user_bio)
    TextView mUserBioView;
    @InjectView(R.id.user_location)
    TextView mUserLocationView;
    @InjectView(R.id.user_homepage)
    TextView mUserHomepageView;
    @InjectView(R.id.user_twitter)
    TextView mUserTwitterView;
    @InjectView(R.id.user_shots)
    TextView mUserShotsView;
    @InjectView(R.id.user_projects)
    TextView mUserProjectsView;
    @InjectView(R.id.user_followers)
    TextView mUserFollowersView;
    @InjectView(R.id.user_followings)
    TextView mUserFollowingsView;
    @InjectView(R.id.user_buckets)
    TextView mUserBucketsView;
    @InjectView(R.id.user_team)
    TextView mUserTeamView;
    @InjectView(R.id.user_likes)
    TextView mUserLikesView;
    private User mUser;
    private boolean mIsFollowing;
    private MenuItem mFollowMenu;
    private ProgressView mMenuProgressView;
    private View mMenuProgressLayout;

    public UserFragment() {
        // Required empty public constructor
    }

    public static UserFragment newInstance(User user) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putParcelable(UiUtils.KEY_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHasTwoPane = getResources().getBoolean(R.bool.two_pane);
        setHasOptionsMenu(!mHasTwoPane);

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(UiUtils.KEY_USER)) {
                mUser = args.getParcelable(UiUtils.KEY_USER);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_layout, container, false);
        ButterKnife.inject(this, view);
        ViewCompat.setTransitionName(mUserImageView, Scene.USER_IMAGE);
        //        ViewCompat.setTransitionName(mUserNameView, Scene.USER_NAME);

        if (mToolbar != null) {
            mToolbar.inflateMenu(R.menu.menu_user);
            mToolbar.setTitle(mUser.name);
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            mToolbar.setOnMenuItemClickListener(this);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
            mFollowMenu = mToolbar.getMenu().findItem(R.id.action_follow);
            mFollowMenu.setVisible(!isSelf());
            if (!isSelf()) {
                checkFollowingStatus();
            }
        }


        setupData();
        return view;
    }

    private boolean isSelf() {
        return mUser.id == Pref.getOAuthUserId(getActivity());
    }

    private void checkFollowingStatus() {
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        startMenuLoading();
        Observable<Response> observable = ApiFactory.getService(getActivity()).checkFollowingUser(String.valueOf(mUser.id));
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((res) -> {
            updateFollowingStatus(res);
        }, (error) -> {
            handleError(error);
        });
    }

    private void startMenuLoading() {
        if (mMenuProgressView == null) {
            initMenuProgressView();
        }
        MenuItemCompat.setActionView(mFollowMenu, R.layout.menu_item_action_refresh);
        MenuItemCompat.expandActionView(mFollowMenu);
        View view = MenuItemCompat.getActionView(mFollowMenu);
        ((ProgressView) view.findViewById(R.id.progress)).start();
    }

    private void stopMenuLoading() {
        MenuItemCompat.collapseActionView(mFollowMenu);
        MenuItemCompat.setActionView(mFollowMenu, null);
    }

    private void initMenuProgressView() {
        mMenuProgressLayout = LayoutInflater.from(getActivity()).inflate(R.layout.menu_item_action_refresh, null);
        mMenuProgressView = (ProgressView) mMenuProgressLayout.findViewById(R.id.progress);
    }

    private void handleError(Throwable error) {
        stopMenuLoading();
        if (error instanceof RetrofitError) {
            RetrofitError re = (RetrofitError) error;
            updateFollowingStatus(re.getResponse());
        }
    }

    private void updateFollowingStatus(Response response) {
        stopMenuLoading();
        if (response.getStatus() == 204) {
            mIsFollowing = true;
        } else if (response.getStatus() == 404) {
            mIsFollowing = false;
        }

        mFollowMenu.setChecked(mIsFollowing);
        mFollowMenu.setTitle(mIsFollowing ? R.string.action_unfollow : R.string.action_follow);
    }

    private void setupData() {
        mUserImageView.setImageURI(Uri.parse(mUser.avatarUrl));
        //        Glide.with(getActivity()).load(mUser.avatarUrl).placeholder(R.drawable.person_image_empty).into(mUserImageView);
        mUserNameView.setText(mUser.name);
        UiUtils.setValueOrHidden(mUserBioView, mUser.bio, true);
        UiUtils.setValueOrHidden(mUserLocationView, mUser.location);
        UiUtils.setValueOrHidden(mUserHomepageView, mUser.links.web);
        UiUtils.setValueOrHidden(mUserTwitterView, mUser.links.twitter);
        if (mHasTwoPane) {
            mUserShotsView.setVisibility(View.GONE);
            mUserProjectsView.setVisibility(View.GONE);
            mUserFollowersView.setVisibility(View.GONE);
            mUserFollowingsView.setVisibility(View.GONE);
            mUserBucketsView.setVisibility(View.GONE);
            mUserTeamView.setVisibility(View.GONE);
            mUserLikesView.setVisibility(View.GONE);
        } else {
            UiUtils.setValueOrHidden(mUserShotsView, mUser.shotsCount, R.plurals.shot_count);
            UiUtils.setValueOrHidden(mUserProjectsView, mUser.projectsCount, R.plurals.project_count);
            UiUtils.setValueOrHidden(mUserFollowersView, mUser.followersCount, R.plurals.follower_count);
            UiUtils.setValueOrHidden(mUserFollowingsView, mUser.followingsCount, R.plurals.following_count);
            UiUtils.setValueOrHidden(mUserBucketsView, mUser.bucketsCount, R.plurals.bucket_count);
            UiUtils.setValueOrHidden(mUserTeamView, mUser.teamsCount, R.plurals.team_count);
            UiUtils.setValueOrHidden(mUserLikesView, mUser.likesCount, R.plurals.like_count);
        }

    }

    //@f:off
    @OnClick({
            R.id.user_location, R.id.user_homepage, R.id.user_twitter,
            R.id.user_shots, R.id.user_projects, R.id.user_followings,
            R.id.user_followers, R.id.user_buckets, R.id.user_team,
            R.id.user_likes})
    //@f:on
    public void onClick(View view) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (activity instanceof View.OnClickListener) {
            ((View.OnClickListener) activity).onClick(view);
            return;
        }


        switch (view.getId()) {
            case R.id.user_location:
                openLocationOnMap();
                break;
            case R.id.user_homepage:
                UiUtils.openUrl(activity, mUser.links.web);
                break;
            case R.id.user_twitter:
                UiUtils.openUrl(activity, mUser.links.twitter);
                break;
            case R.id.user_shots:
                UiUtils.launchUserShots(activity, mUser);
                break;
            case R.id.user_projects:
                UiUtils.launchUserProjects(activity, mUser);
                break;
            case R.id.user_followers:
                UiUtils.launchUserFollowers(activity, mUser);
                break;
            case R.id.user_followings:
                UiUtils.launchUserFollowings(activity, mUser);
                break;
            case R.id.user_buckets:
                UiUtils.launchUserBuckets(activity, mUser);
                break;
            case R.id.user_team:
                UiUtils.launchUserTeams(activity, mUser);
                break;
            case R.id.user_likes:
                UiUtils.launchUserLikes(activity, mUser);
                break;
        }

    }

    private void openLocationOnMap() {
        UiUtils.launchMap(getActivity(), mUser.location);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_user, menu);
        mFollowMenu = menu.findItem(R.id.action_follow);
        mFollowMenu.setVisible(!isSelf());
        if (!isSelf()) {
            checkFollowingStatus();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_follow) {
            followMenuClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_follow) {
            followMenuClicked();
            return true;
        }
        return false;
    }

    private void checkUnfollowingRes(Response res) {
        stopMenuLoading();

        if (res.getStatus() == 204) {
            swapFollowStatus();
        }
    }

    private void checkFollowingRes(Response res) {
        stopMenuLoading();
        if (res.getStatus() == 204) {
            swapFollowStatus();
        }
        TypedInput body = res.getBody();
    }

    private void handleFollowingError(Throwable error) {
        stopMenuLoading();
        if (error instanceof RetrofitError) {
            RetrofitError re = (RetrofitError) error;
            checkFollowingError(re.getResponse());
        }
    }

    private void swapFollowStatus() {
        mIsFollowing = !mIsFollowing;

        mFollowMenu.setChecked(mIsFollowing);
        mFollowMenu.setTitle(mIsFollowing ? R.string.action_unfollow : R.string.action_follow);
    }

    private void checkFollowingError(Response res) {
        String error = ApiFactory.getErrorMessage(res);

    }

    private void followMenuClicked() {
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiFactory.getService(getActivity());
        String id = String.valueOf(mUser.id);
        startMenuLoading();
        if (mIsFollowing) {
            service.unfollowUser(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((res) -> {
                checkUnfollowingRes(res);
            }, new ErrorCallback(getActivity()));
        } else {
            service.followUser(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((res) -> {
                checkFollowingRes(res);
            }, (error) -> {
                handleFollowingError(error);
            });
        }

    }
}

