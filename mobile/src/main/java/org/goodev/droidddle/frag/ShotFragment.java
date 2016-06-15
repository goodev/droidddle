package org.goodev.droidddle.frag;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.transition.Transition;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import org.goodev.design.transition.AnimUtils;
import org.goodev.droidddle.BuildConfig;
import org.goodev.droidddle.R;
import org.goodev.droidddle.ShotDetailsActivity;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ApiService;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.api.SucessCallback;
import org.goodev.droidddle.frag.shot.ShotCommentFragment;
import org.goodev.droidddle.pojo.Comment;
import org.goodev.droidddle.pojo.Project;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.Ads;
import org.goodev.droidddle.utils.FileUtils;
import org.goodev.droidddle.utils.FrescoUtils;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.Scene;
import org.goodev.droidddle.utils.ShotPref;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.GoURLSpan;
import org.goodev.droidddle.widget.ParallaxScrollListener;
import org.goodev.droidddle.widget.ThemeListPreference;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import butterknife.Optional;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.animation.GlideAnimation;
//import com.bumptech.glide.request.target.BaseTarget;
//import com.bumptech.glide.request.target.SizeReadyCallback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShotFragment extends StatFragment implements /*ObservableScrollViewCallbacks,*/ Toolbar.OnMenuItemClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_SHOT = "extra_shot";
    private static final String ARG_ANIMATION = "param2";
    private static final int REQ_CODE_PICK_IMAGE = 1;
    @Optional
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    boolean mHasTwoPane;
    @InjectView(R.id.user_image)
    SimpleDraweeView mUserImageView;
    @InjectView(R.id.user_name)
    TextView mUserNameView;
    @InjectView(R.id.shot_date)
    TextView mShotDateView;
    @InjectView(R.id.shot_views)
    TextView mShotViewsView;
    @InjectView(R.id.shot_image)
    SimpleDraweeView mShotImageView;

    @Optional
    @InjectView(R.id.shot_comment)
    Button mShotCommentView;
    @Optional
    @InjectView(R.id.shot_attachment)
    Button mShotAttachmentView;
    @Optional
    @InjectView(R.id.shot_likes)
    Button mShotLikesView;
    @Optional
    @InjectView(R.id.shot_buckets)
    Button mShotBucketsView;
    @Optional
    @InjectView(R.id.shot_rebound)
    Button mShotReboundView;
    @Optional
    @InjectView(R.id.shot_project)
    Button mShotProjectView;
    @InjectView(R.id.shot_rebound_source)
    Button mShotReboundSourceView;
    @InjectView(R.id.shot_title)
    TextView mShotTitleView;
    @InjectView(R.id.shot_description)
    TextView mShotDescriptionView;
    @InjectView(R.id.shot_tag_layout)
    ViewGroup mShotTagLayout;
    @Optional
    @InjectView(R.id.fab)
    FloatingActionButton mFab;
    @InjectViews({R.id.palette_1, R.id.palette_2, R.id.palette_3, R.id.palette_4, R.id.palette_5, R.id.palette_6, R.id.palette_7,
            R.id.palette_8})
    ImageView[] mPaletteViews;
    MenuItem mLikeMenu;
    List<Project> mProjects;
    // TODO: Rename and change types of parameters
    private Shot mShot;
    private boolean mAnimation;
    private ParallaxScrollListener mScrollListener;
    private View.OnClickListener mOnClickListener;
    private boolean mIsSelf;
    private File mFile;
    private Dialog mDialog;

    @Optional
    @InjectView(R.id.shot_content)
    RelativeLayout mShotContentLayout;

    public ShotFragment() {
        // Required empty public constructor
    }

    public static ShotFragment newInstance(Shot shot, int color, boolean anim) {
        ShotFragment fragment = new ShotFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SHOT, shot);
        args.putInt(ShotCommentFragment.ARG_COLOR, color);
        args.putBoolean(ARG_ANIMATION, anim);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColor = getArguments().getInt(ShotCommentFragment.ARG_COLOR);
        }
        mHasTwoPane = getResources().getBoolean(R.bool.two_pane);
        setHasOptionsMenu(!mHasTwoPane);
        if (getArguments() != null) {
            mShot = getArguments().getParcelable(ARG_SHOT);
            mAnimation = getArguments().getBoolean(ARG_ANIMATION, false);
            getActivity().setTitle(mShot.title);
            mIsSelf = OAuthUtils.isSelf(mShot.user.id);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ParallaxScrollListener) {
            mScrollListener = (ParallaxScrollListener) activity;
        }
        if (activity instanceof View.OnClickListener) {
            mOnClickListener = (View.OnClickListener) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shot_layout, container, false);
        ButterKnife.inject(this, view);
        if (mCommentEditView != null) {
            setupCommentEditView();
        }

        if (UiUtils.hasLollipop() && mFab != null && mAnimation) {
            mFab.setAlpha(0f);
            getActivity().getWindow().getSharedElementEnterTransition()
                    .addListener(new AnimUtils.TransitionListenerAdapter() {
                        @Override
                        public void onTransitionEnd(Transition transition) {
                            animateFabIn();
                        }
                    });
            //由于没有单独设置 退出的 transition 所以和上面的一样
//            getActivity().getWindow().getSharedElementReturnTransition()
//                    .addListener(new AnimUtils.TransitionListenerAdapter(){
//                        @Override
//                        public void onTransitionStart(Transition transition) {
//                            animateFabOut();
//                        }
//
//                    });
        }
        ViewCompat.setTransitionName(mShotImageView, Scene.SHOT_IMAGE);
        FrescoUtils.setShotHierarchy(getActivity(), mShotImageView);
        //        ViewCompat.setTransitionName(mShotTitleView, Scene.SHOT_TITLE);
        //        ViewCompat.setTransitionName(mShotDescriptionView, Scene.SHOT_DESCRIPTION);

        if (mToolbar != null) {
            mToolbar.inflateMenu(R.menu.menu_shot);
            mToolbar.setTitle(mShot.title);
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            mToolbar.setOnMenuItemClickListener(this);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
            boolean isPro = mShot.user.pro;
            mToolbar.getMenu().findItem(R.id.action_add_attachment).setVisible(mIsSelf && isPro);
        }

        updateData();
//        mObservableScrollView.setScrollViewCallbacks(this);
        boolean showAds = Pref.isShowHomeAds(getActivity());
        if (showAds && !BuildConfig.IS_PLAY) {
            Ads.setupAds(getActivity(), mShotContentLayout);
        }
        return view;
    }

    private void animateFabIn() {
        mFab.setScaleX(0f);
        mFab.setScaleY(0f);
        mFab.animate().alpha(1f).scaleX(1f).scaleY(1f).setInterpolator(new AccelerateInterpolator())
                .setDuration(300).start();

//        setFinishFabOutTransition();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setFinishFabOutTransition() {
        getActivity().getWindow().getSharedElementReturnTransition()
                .addListener(new AnimUtils.TransitionListenerAdapter() {
                    @Override
                    public void onTransitionStart(Transition transition) {
                        animateFabOut();
                    }

                });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateFabOut() {
        mFab.setVisibility(View.INVISIBLE);
        mShotImageView.setElevation(1f);
    }

    public void finish() {
        if (mFab == null || !mAnimation) {
            ViewCompat.setElevation(mShotImageView, 1f);
            if (getActivity() != null) {
                getActivity().supportFinishAfterTransition();
            }
            return;
        }
        mFab.animate().alpha(0f).scaleX(0f).scaleY(0f).setInterpolator(new AccelerateInterpolator())
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewCompat.setElevation(mShotImageView, 1f);
                        if (getActivity() != null) {
                            getActivity().supportFinishAfterTransition();
                        }
                    }
                })
                .start();


    }

    @Optional
    @OnClick({R.id.shot_comment, R.id.shot_likes, R.id.shot_attachment, R.id.shot_buckets, R.id.shot_rebound, R.id.shot_project, R.id.shot_rebound_source, R.id.user_image})
    void onClickComment(View view) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(view);
        }
    }

    @Optional
    @OnClick({R.id.shot_image, R.id.fab})
    void onClickImage(View view) {
        switch (view.getId()) {
            case R.id.shot_image:
                UiUtils.launchShotImage(getActivity(), mShot);
                break;
            case R.id.fab:
                likeOrUnlike(view);
                break;
        }
    }

    private void likeOrUnlike(View view) {
        boolean checked = view.isSelected();
        view.setSelected(!checked);
        likeOrUnlikeShot(!checked);
    }

    private void updateData() {
        final Shot shot = mShot;
        mUserImageView.setImageURI(Uri.parse(shot.user.avatarUrl));
        if (shot.images.hidpi != null) {
//            loadPalette(shot.images.normal);
            FrescoUtils.setShotUrl(mShotImageView, shot.images.hidpi, shot.images.normal);
        } else {
//            loadPalette(shot.images.teaser);
            FrescoUtils.setShotUrl(mShotImageView, shot.images.normal, shot.images.teaser);
        }
//        mShotImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                float ratio = (float)mShotImageView.getWidth() / mShotImageView.getHeight();
//                L.e("..... ratio %s", ratio);
//                if(ratio - 1.33f <= 0.001f){
//                    mShotImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                    startSharedAnimation();
//                }
//            }
//        });

//        Glide.with(mContext).load(shot.user.avatarUrl).placeholder(R.drawable.person_image_empty).into(mUserImageView);
//        Glide.with(mContext).load(shot.images.normal).placeholder(R.drawable.placeholder).fitCenter().into(mShotImageView);

        if (!mHasTwoPane) {
            String comments = String.valueOf(shot.commentsCount.intValue());
            mShotCommentView.setText(comments);
            String likes = UiUtils.intToString(shot.likesCount);
            mShotLikesView.setText(likes);
            String buckets = UiUtils.intToString(shot.bucketsCount);
            mShotBucketsView.setText(buckets);

            setValueIfHave(mShotReboundView, shot.reboundsCount.intValue());
            setValueIfHave(mShotAttachmentView, shot.attachmentsCount.intValue(), mIsSelf);

            //TODO get projects count...
            updateProjects();
        }

        //        mShotLikeView.setChecked(shot.);
        //        mShotLikeView.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(final View v) {
        //                ToggleButton toggleButton = (ToggleButton) v;
        //                if (toggleButton.isChecked()) {
        //                    Animation likeAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.like_btn_anim);
        //                    Animation scaleAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.like_scale_anim);
        //                    AnimationSet animSet = new AnimationSet(false);
        //                    animSet.addAnimation(likeAnim);
        //                    animSet.addAnimation(scaleAnim);
        //
        //                    v.startAnimation(scaleAnim);
        //                }
        //            }
        //        });

        mUserNameView.setText(shot.user.name);
        mShotViewsView.setText(String.valueOf(shot.viewsCount));
        //        CharSequence date = DateUtils.getRelativeDateTimeString(mContext,shot.createdAt.getTime(),DateUtils.DAY_IN_MILLIS,DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE|DateUtils.FORMAT_NUMERIC_DATE);
        //        CharSequence date = DateUtils.formatDateTime(mContext,shot.createdAt.getTime(),DateUtils.FORMAT_NUMERIC_DATE);
        CharSequence date = DateUtils.getRelativeTimeSpanString(shot.createdAt.getTime(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE | DateUtils.FORMAT_NUMERIC_DATE);
        mShotDateView.setText(date);
        mShotTitleView.setText(shot.title);
        if (TextUtils.isEmpty(shot.description)) {
            mShotDescriptionView.setText(null);
            mShotDescriptionView.setVisibility(View.INVISIBLE);
        } else {
            mShotDescriptionView.setVisibility(View.VISIBLE);
            Spanned spannable = GoURLSpan.hackURLSpan(Html.fromHtml(shot.description));
            mShotDescriptionView.setText(spannable);
            mShotDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
            mShotDescriptionView.requestLayout();
        }
        if (TextUtils.isEmpty(shot.reboundSourceUrl)) {
            mShotReboundSourceView.setVisibility(View.GONE);
        } else {
            mShotReboundSourceView.setVisibility(View.VISIBLE);
        }

        //TODO tablet support first? then this?
        if (false && getActivity() instanceof ShotDetailsActivity) {

//            Glide.with(mContext).load(shot.images.teaser).asBitmap().into(new BaseTarget<Bitmap>() {
//                @Override
//                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                    L.d("........ tint image resource " + resource);
//                    ShotDetailsActivity activity = (ShotDetailsActivity) getActivity();
//                    activity.extractAndApplyTintFromPhotoViewAsynchronously(resource);
//                }
//
//                @Override
//                public void getSize(SizeReadyCallback cb) {
//                    cb.onSizeReady(400, 300);
//                }
//            });
        }

        updateShotTags(shot.tags);
        loadAcoPalette();
    }

//    private void startSharedAnimation() {
//        if(getActivity() instanceof DetailsActivity){
//            mShotImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//                @Override
//                public boolean onPreDraw() {
//                    mShotImageView.getViewTreeObserver().removeOnPreDrawListener(this);
//                    L.e("shot image view height : %s  width %s", mShotImageView.getHeight(), mShotImageView.getWidth());
//                    DetailsActivity a = (DetailsActivity) getActivity();
//                    a.supportStartPostponedEnterTransition();
//                    return true;
//                }
//            });
//        }
//    }

    private void updateShotTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            mShotTagLayout.setVisibility(View.GONE);
        } else {
            int size = tags.size();
            for (int i = 0; i < size; i++) {
                addTagButton(mShotTagLayout, tags.get(i));

            }
        }
    }

    private void addTagButton(ViewGroup shotTagLayout, final String s) {
        Button button = (Button) LayoutInflater.from(getActivity()).inflate(R.layout.shot_tag, shotTagLayout, false);
        button.setText(s);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.openTagShotActivity(getActivity(), s);
            }
        });
        shotTagLayout.addView(button);
    }

    private void updateProjects() {
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<List<Project>> projectsObservable = ApiFactory.getService(getActivity()).getShotProjects(mShot.id, 1);
        projectsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((p) -> {
            updateProject(p);
        }, new ErrorCallback(getActivity()));
    }

    private void updateProject(List<Project> projects) {
        mProjects = projects;
        setValueIfHave(mShotProjectView, projects.size());
    }

    private void setValueIfHave(Button button, int value) {
        setValueIfHave(button, value, false);
    }

    private void setValueIfHave(Button button, int value, boolean allwaysShow) {
        if (value == 0 && !allwaysShow) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
        }
        String text = String.valueOf(value);
        button.setText(text);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_shot, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean isPro = mShot.user.pro;
        menu.findItem(R.id.action_add_attachment).setVisible(mIsSelf && isPro);
        //TODO 在平板上 ， fab 没有， 则显示 菜单
        mLikeMenu = menu.findItem(R.id.action_like);
        mLikeMenu.setVisible(mFab == null);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                UiUtils.shareShot(getActivity(), mShot);
                break;
            case R.id.action_add_attachment:
                startPickFile();
                break;
            case R.id.action_download:
                saveImage();
                break;

            case R.id.action_add_to_bucket:
                UiUtils.launchAddToBucket(getActivity(), mShot);
                break;

            case R.id.action_like:
                boolean checked = item.isChecked();
                item.setChecked(!checked);
                if (!checked) {
                    item.setIcon(R.drawable.ic_action_liked);
                } else {
                    item.setIcon(R.drawable.ic_action_like);
                }
                likeOrUnlikeShot(!checked);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImage() {
        if (mShot == null || mShot.images == null || mShot.images.hidpi == null) {
            return;
        }
        Context mContext = getActivity();
        if (UiUtils.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            UiUtils.downloadFile(mContext, mShot.images.hidpi, mShot.id);
        } else {
            Toast.makeText(mContext, R.string.sdcard_permission_tips, Toast.LENGTH_LONG).show();
            UiUtils.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
        }
    }

    private void startPickFile() {
        UiUtils.showToast(getActivity(), R.string.pick_file_tips);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQ_CODE_PICK_IMAGE) {
            Uri uri = data.getData();
            String filePath = FileUtils.getPath(getActivity(), uri);
            mFile = new File(filePath);
            if (!mFile.exists()) {
                UiUtils.showToast(getActivity(), R.string.attach_file_is_missing);
            }
            if (mFile.length() > ApiService.MAX_ATTACHMENT_LENGTH) {
                UiUtils.showToast(getActivity(), R.string.attach_file_is_large);
            }
            showUploadAttachmentDialog();
        }
    }
    //@f:on

    //@f:off
    private void showUploadAttachmentDialog() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.upload_attachment_title)
                .setMessage(getActivity()
                        .getString(R.string.upload_attachment_message, mFile.getName()))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uploadAttachment();
                    }
                }).setNegativeButton(android.R.string.cancel, null).setCancelable(false)
                .show();
        //        new MaterialDialog.Builder(getActivity())
        //                .title(R.string.upload_attachment_title)
        //                .content(R.string.upload_attachment_message, mFile.getName())
        //                .positiveText(android.R.string.ok)
        //                .negativeText(android.R.string.cancel)
        //                .cancelable(false)
        //                .callback(new MaterialDialog.SimpleCallback() {
        //                    @Override
        //                    public void onPositive(MaterialDialog materialDialog) {
        //                        uploadAttachment();
        //                    }})
        //                .show();
    }

    private void uploadAttachment() {
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Context mContext = getActivity();
        mDialog = UiUtils.showProgressDialog(mContext, getString(R.string.creating_attachment));
        TypedFile file = new TypedFile(FileUtils.getMimeType(mFile), mFile);
        Observable<Response> observable = ApiFactory.getService(mContext).createShotAttachments(mShot.id, file);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SucessCallback<Response>(mContext, R.string.create_attachment_success, mDialog), new ErrorCallback(mContext, mDialog));
    }

    private void updateLikeView(boolean checked) {
        if (mFab != null) {
            mFab.setSelected(checked);
        } else {
            mLikeMenu.setChecked(checked);
        }
    }

    private void likeOrUnlikeShot(boolean checked) {
        Context mContext = getActivity();
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            updateLikeView(!checked);
            return;
        }
        ApiService service = ApiFactory.getService(mContext);
        Observable<Response> observable;
        int res;
        if (checked) {
            observable = service.likeShot(mShot.id);
            res = R.string.like_shot_success;
        } else {
            res = R.string.unlike_shot_success;
            observable = service.unlikeShot(mShot.id);
        }
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SucessCallback<Response>(mContext, res), new ErrorCallback(mContext) {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        updateLikeView(!checked);
                    }
                });
    }

    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (mScrollListener != null && !mHasTwoPane) {
            mScrollListener.onScrollChanged(scrollY, firstScroll, dragging, mShotImageView, mShotImageView.getHeight());
        }
    }


    public Drawable getShotDrawable() {
        if (mShotImageView == null)
            return null;
        return mShotImageView.getDrawable();
    }


    private void loadPalette(String url) {
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url)).build();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(imageRequest, this);
        dataSource.subscribe(new BaseBitmapDataSubscriber() {
                                 @Override
                                 public void onNewResultImpl(Bitmap bitmap) {
                                     // You can use the bitmap in only limited ways
                                     // No need to do any cleanup.
                                     //http://stackoverflow.com/questions/28144847/differences-between-android-palette-colors
                                     //http://www.exoguru.com/android/ui/widgets/extracting-colors-from-images-using-palette-library.html
                                     Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                         @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                         public void onGenerated(Palette palette) {
                                             updateShotPalette(palette);
                                         }
                                     });

                                 }

                                 @Override
                                 public void onFailureImpl(DataSource dataSource) {
                                     // No cleanup required here.
                                 }
                             },
                CallerThreadExecutor.getInstance());

    }

    float mDensity;

    private void updateShotPalette(Palette palette) {
        mDensity = getResources().getDisplayMetrics().density;
        int index = 0;
        Palette.Swatch swatch = palette.getDarkMutedSwatch();
        if (swatch != null) {
            setPaletteColor(index, swatch);
            index++;
        }
        swatch = palette.getDarkVibrantSwatch();
        if (swatch != null) {
            setPaletteColor(index, swatch);
            index++;
        }
        swatch = palette.getMutedSwatch();
        if (swatch != null) {
            setPaletteColor(index, swatch);
            index++;
        }
        swatch = palette.getVibrantSwatch();
        if (swatch != null) {
            setPaletteColor(index, swatch);
            index++;
        }
        swatch = palette.getLightMutedSwatch();
        if (swatch != null) {
            setPaletteColor(index, swatch);
            index++;
        }

        swatch = palette.getLightVibrantSwatch();
        if (swatch != null) {
            setPaletteColor(index, swatch);
            index++;
        }

    }

    private void setPaletteColor(int index, Palette.Swatch swatch) {
        mPaletteViews[index].setImageBitmap(ThemeListPreference.getPreviewBitmap(mDensity, swatch.getRgb()));
        String color = String.format("%06X", 0xFFFFFF & swatch.getRgb());
        mPaletteViews[index].setTag(color);
    }


    @OnClick({R.id.palette_1, R.id.palette_2, R.id.palette_3, R.id.palette_4, R.id.palette_5, R.id.palette_6, R.id.palette_7,
            R.id.palette_8})
    void onPaletteClick(View view) {
        String color = (String) view.getTag();
        if (TextUtils.isEmpty(color)) {
            return;
        }
        UiUtils.launchColorSearchActivity(getActivity(), color);
    }

    public void loadAcoPalette() {
        Observable<Pair<String, Bitmap>[]> observable = Observable.create(new Observable.OnSubscribe<Pair<String, Bitmap>[]>() {
            @Override
            public void call(Subscriber<? super Pair<String, Bitmap>[]> subscriber) {
                byte[] data = ApiFactory.downloadAco(getActivity(), mShot.id);
                subscriber.onNext(parseAco(data));
                subscriber.onCompleted();
            }
        });
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            setAcoViews(data);
        }, new ErrorCallback(getActivity()));
    }

    //https://dribbble.com/shots/2206903/colors.aco
    // parse color will take a lot of time
    private Pair<String, Bitmap>[] parseAco(byte[] data) {
        Pair<String, Bitmap>[] colors = null;
        mDensity = getResources().getDisplayMetrics().density;
        if (data == null || data.length < 5) {
            return colors;
        }
        if (data[1] != 1) {
            return colors;
        }

        int length = data[3] > 8 ? 8 : data[3];
        int index = 3;
        colors = new Pair[length];
        for (int i = 0; i < length && index + 10 < data.length; i++) {
            index += 4;
            byte red = data[index];
            index += 2;
            byte g = data[index];
            index += 2;
            byte bb = data[index];
            index += 2;
            String color = String.format("%02x", red & 0x0000FF) + String.format("%02x", g & 0x0000FF)
                    + String.format("%02x", bb & 0x0000FF);
//            colors[i] = Integer.parseInt(color, 16);
            colors[i] = new Pair<>(color, ThemeListPreference.getPreviewBitmap(mDensity, "#" + color));
//            mPaletteViews[i].setImageBitmap(ThemeListPreference.getPreviewBitmap(mDensity, "#" + color));
//            mPaletteViews[i].setTag(color);
        }

        return colors;
    }

    public void setAcoViews(Pair<String, Bitmap>[] bms) {
        if (bms == null) {
            return;
        }
        int length = bms.length;
        for (int i = 0; i < length; i++) {
            Pair<String, Bitmap> color = bms[i];
            mPaletteViews[i].setImageBitmap(color.second);
            mPaletteViews[i].setTag(color.first);
        }
    }


    @Optional
    @InjectView(R.id.comment_edit)
    AutoCompleteTextView mCommentEditView;
    @Optional
    @InjectView(R.id.comment_send)
    ImageButton mSendButton;

    private Observable<Comment> mPostCommentObservable;
    private int mColor;

    @Optional
    @OnClick(R.id.comment_send)
    void onSendButtonClicked(View view) {
        String text = mCommentEditView.getText().toString().trim();
        onPostComment(text);
    }

    public void onPostComment(String text) {
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        String message = getString(R.string.sending);
        mDialog = UiUtils.showProgressDialog(getActivity(), message);

        ShotPref.saveComment(mShot.id, text);
        if (Pref.getSendByComment(getActivity())) {
            text += getString(R.string.send_by_text);
        }
        mPostCommentObservable = ApiFactory.getService(getActivity()).postShotComments(mShot.id, text);
        mPostCommentObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((comment) -> {
            commentAdded(comment);
        }, (error) -> {
            commentError(error);
        });
    }

    private void commentError(Throwable error) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        UiUtils.toastError(activity, error);
    }

    private void commentAdded(Comment comment) {
        ShotPref.removeComment(mShot.id);
        UiUtils.dismissDialog(mDialog);
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        mCommentEditView.setText(null);
        Toast.makeText(getActivity(), R.string.comment_added, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCommentEditView != null) {
            String text = mCommentEditView.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                ShotPref.saveComment(mShot.id, text);
            }
        }
    }

    private void setupCommentEditView() {
        String[] data = getResources().getStringArray(R.array.comment_quick_reply);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, data);
        mCommentEditView.setAdapter(adapter);
        mCommentEditView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mCommentEditView.getText().toString())) {
                    mCommentEditView.showDropDown();
                }
            }
        });
//        UiUtils.setupCommentPopupTips(getActivity(), mCommentEditView, new OnFilterListener() {
//            @Override
//            public void update(int pos) {
//                mCommentEditView.setText(data[pos]);
//            }
//        }, data);

        mCommentEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButtonStatus(s);
            }
        });

        String comment = ShotPref.getComment(mShot.id);
        if (!TextUtils.isEmpty(comment)) {
            mCommentEditView.setText(comment);
        } else {
            mSendButton.setEnabled(false);
        }
    }


    private void updateSendButtonStatus(Editable s) {
        String text = s.toString().trim();
        if (TextUtils.isEmpty(text)) {
            mSendButton.setEnabled(false);
            mSendButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
        } else {
            mSendButton.setColorFilter(mColor, PorterDuff.Mode.SRC_ATOP);
            mSendButton.setEnabled(true);
        }
    }

}
