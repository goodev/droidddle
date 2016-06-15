package org.goodev.droidddle;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v7.graphics.Palette;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.frag.ShotFragment;
import org.goodev.droidddle.frag.shot.ObservableFragment;
import org.goodev.droidddle.frag.shot.ShotAttachmentFragment;
import org.goodev.droidddle.frag.shot.ShotBucketFragment;
import org.goodev.droidddle.frag.shot.ShotCommentFragment;
import org.goodev.droidddle.frag.shot.ShotCommentFragment.OnCommentActionListener;
import org.goodev.droidddle.frag.shot.ShotLikeFragment;
import org.goodev.droidddle.frag.shot.ShotProjectFragment;
import org.goodev.droidddle.frag.shot.ShotReboundFragment;
import org.goodev.droidddle.pojo.Comment;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.MaterialColorMapUtils;
import org.goodev.droidddle.utils.MaterialColorMapUtils.MaterialPalette;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.ThemeUtil;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.utils.WhitenessUtils;
import org.goodev.droidddle.widget.ParallaxScrollListener;
import org.goodev.droidddle.widget.ProgressView;

import butterknife.InjectView;
import butterknife.Optional;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ShotDetailsActivity extends DetailsActivity<Shot> implements ParallaxScrollListener, OnClickListener, OnCommentActionListener {

    long mShotId;
    boolean mOpenComment;

    private ShotCommentFragment mCommentFragment;
    private ShotLikeFragment mLikeFragment;
    private ShotBucketFragment mBucketFragment;
    private ShotProjectFragment mProjectFragment;
    private ShotAttachmentFragment mAttachmentFragment;
    private ShotReboundFragment mReboundFragment;
    private ObservableFragment mCurrentFragment;
    private boolean mIsSelf;
    private ShotFragment mShotFragment;
    private MaterialColorMapUtils mMaterialColorMapUtils;
    private boolean mHasComputedThemeColor;
    private int mStatusBarColor;
    private PorterDuffColorFilter mColorFilter;

    @Override
    protected int getLayoutRes() {
        return R.layout.shot_detalis_layout;
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.home:
            case android.R.id.home:
                if (needSlidDownTabs()) {
                    slidDownTabs();
                    return true;
                }
                if (mShotFragment != null && UiUtils.hasLollipop()) {
                    mShotFragment.finish();
                } else {
                    supportFinishAfterTransition();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mHasTwoPane && needSlidDownTabs()) {
            slidDownTabs();
            return;
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.sliding_content);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        if (mShotFragment != null && UiUtils.hasLollipop()) {
            mShotFragment.finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void initParams() {
        Bundle extra = getIntent().getExtras();
        mOpenComment = extra.getBoolean(UiUtils.ARG_COMMENT, false);
        if (extra.containsKey(ShotFragment.ARG_SHOT)) {
            mData = extra.getParcelable(ShotFragment.ARG_SHOT);
            //Reload this shot
            if (mData.user == null) {
                mShotId = mData.id;
                mData = null;
            } else {
                mIsSelf = OAuthUtils.isSelf(mData.user.id);
            }
        } else if (extra.containsKey(UiUtils.ARG_SHOT_ID)) {
            mShotId = extra.getLong(UiUtils.ARG_SHOT_ID, UiUtils.NO_ID);
        } else {
            finish();
        }
    }

    @Override
    protected void onPostMyCreate(Bundle savedInstanceState) {
        super.onPostMyCreate(savedInstanceState);
//        if(UiUtils.hasLollipop()){
//            getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.dribbble_shot_enter));
//        }
        if (savedInstanceState == null) {
            if (mData != null) {
                addShotFragment(true);
            } else if (mShotId != UiUtils.NO_ID) {
                loadShotData();
            }
        }

    }

    @Override
    protected PagerAdapter getViewPagerAdapter() {
        return new ShotDetailsPagerAdapter(this, getSupportFragmentManager(), mData, mIsSelf);
    }

    private void loadShotData() {
        if (!Utils.hasInternet(this)) {
            Toast.makeText(this, R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        startLoadingView();
        Observable<Shot> observable = ApiFactory.getService(this).getShot(mShotId);
        observable.subscribeOn(Schedulers.io()).subscribe((shot) -> {
            updateShot(shot);
        }, new ErrorCallback(this));
    }

    private void updateShot(Shot shot) {
        hideLoadingView();
        mData = shot;
        mIsSelf = OAuthUtils.isSelf(shot.user.id);
        addShotFragment(false);
        if (mHasTwoPane) {
            mTabs.post(new Runnable() {
                @Override
                public void run() {
                    addViewPager();
                }
            });

        }
    }

    private void addShotFragment(boolean sharedAnim) {
        mShotFragment = ShotFragment.newInstance(mData, mPrimaryColor, sharedAnim);
        getSupportFragmentManager().beginTransaction().add(R.id.content_content, mShotFragment).commit();

        if (!mHasTwoPane && mOpenComment) {
            //openCommentTab();
        }

//  TODO
//        if (UiUtils.hasLollipop()) {
//            loadPalette();
//        }
    }

    private void loadPalette() {
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(mData.images.teaser)).build();
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
                                             int defaultColor = ThemeUtil.getThemeColor(ShotDetailsActivity.this, R.attr.colorPrimaryDark);
                                             Palette.Swatch vibrant = palette.getVibrantSwatch();
                                             if (vibrant != null) {
                                                 mToolbar.setBackgroundColor(vibrant.getRgb());
                                                 mToolbar.setTitleTextColor(vibrant.getTitleTextColor());
                                             }
//                                             mToolbar.setBackgroundColor(palette.getMutedColor(defaultColor));
//                                             mToolbar.setTitleTextColor(palette.getLightVibrantColor(Color.WHITE));
                                             Palette.Swatch darkVibrant = palette.getDarkVibrantSwatch();
                                             if (darkVibrant != null) {
                                                 mDrawerLayout.setStatusBarBackgroundColor(0xFF000000 | darkVibrant.getRgb());
                                                 getWindow().setNavigationBarColor(darkVibrant.getRgb());
//                                                     getWindow().setStatusBarColor(darkVibrant.getRgb());
                                             }
                                         }
                                     });

                                 }

                                 @Override
                                 public void onFailureImpl(DataSource dataSource) {
                                     // No cleanup required here.
                                 }
                             },
                CallerThreadExecutor.getInstance());

//        CloseableReference<CloseableImage> imageReference = null;
//        try {
//            imageReference = dataSource.getResult();
//            if (imageReference != null) {
//                CloseableImage image = imageReference.get();
//                // do something with the image
//            }
//        } finally {
//            dataSource.close();
//            CloseableReference.closeSafely(imageReference);
//        }
    }


    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging, View parallaxView, int parallaxHeight) {
        if (parallaxHeight > mActionBarSize)
            parallaxHeight -= mActionBarSize;
        parallaxView.setTranslationY(scrollY / 2);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shot_comment:
                openCommentTab();
                break;
            case R.id.shot_attachment:
                openAttachmentTab();
                break;
            case R.id.shot_likes:
                openLikeTab();
                break;
            case R.id.shot_buckets:
                openBucketsTab();
                break;
            case R.id.shot_project:
                openProjectTab();
                break;
            case R.id.shot_rebound:
                openReboundTab();
                break;
            case R.id.user_image:
                UiUtils.launchUser(this, mData.user, v);
                break;
            case R.id.shot_rebound_source:
                launchReboundSourceShot();
                break;
        }
    }

    private void launchReboundSourceShot() {
        long id = UiUtils.getReboundSourceId(mData);
        if (id != UiUtils.NO_ID) {
            UiUtils.launchShot(this, id);
        }
    }

    private void openReboundTab() {
        mToolbar.setSubtitle(getString(R.string.rebounds_text, mData.reboundsCount));
        if (mReboundFragment == null) {
            mReboundFragment = ShotReboundFragment.newInstance(mData.id, 0);
        }
        if (mCurrentFragment != mReboundFragment) {
            mCurrentFragment = mReboundFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mCurrentFragment).commit();
        }
        scrollToTop();

    }

    private void openProjectTab() {
        mToolbar.setSubtitle(getString(R.string.projects_text));
        if (mProjectFragment == null) {
            mProjectFragment = ShotProjectFragment.newInstance(mData.id, 0);
        }
        if (mCurrentFragment != mProjectFragment) {
            mCurrentFragment = mProjectFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mCurrentFragment).commit();
        }
        scrollToTop();
    }

    private void openBucketsTab() {
        if (mData.bucketsCount == null) {
            mData.bucketsCount = 0;
        }
        mToolbar.setSubtitle(getString(R.string.buckets_text, mData.bucketsCount));
        if (mBucketFragment == null) {
            mBucketFragment = ShotBucketFragment.newInstance(mData.id, mData.bucketsCount);
        }
        if (mCurrentFragment != mBucketFragment) {
            mCurrentFragment = mBucketFragment;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sliding_content, mCurrentFragment).commit();
        }
        scrollToTop();
    }

    private void openLikeTab() {
        mToolbar.setSubtitle(getString(R.string.likes_text, mData.likesCount));
        if (mLikeFragment == null) {
            mLikeFragment = ShotLikeFragment.newInstance(mData.id, "");
        }
        if (mCurrentFragment != mLikeFragment) {
            mCurrentFragment = mLikeFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mCurrentFragment).commit();
        }
        scrollToTop();
    }

    private void openAttachmentTab() {
        mToolbar.setSubtitle(getString(R.string.attachment_text, mData.attachmentsCount));
        if (mAttachmentFragment == null) {
            mAttachmentFragment = ShotAttachmentFragment.newInstance(mData.id, mIsSelf);
        }
        if (mCurrentFragment != mAttachmentFragment) {
            mCurrentFragment = mAttachmentFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mCurrentFragment).commit();
        }
        scrollToTop();
    }

    private void openCommentTab() {
        mToolbar.setSubtitle(getString(R.string.comments_text, mData.commentsCount));
        if (mCommentFragment == null) {
            mCommentFragment = ShotCommentFragment.newInstance(mData.id, mPrimaryColor);
        }
        if (mCurrentFragment != mCommentFragment) {
            mCurrentFragment = mCommentFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_content, mCommentFragment).commit();
        }
        scrollToTop();
    }


    private long getShotId() {
        if (mData != null)
            return mData.id;

        return mShotId;
    }

    // shot comment actions
    @Override
    public void onPostComment(String text) {
        if (!Utils.hasInternet(this)) {
            Toast.makeText(this, R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<Comment> observable = ApiFactory.getService(this).postShotComments(getShotId(), text);
        observable.observeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread()).subscribe((comment) -> {
            commentAdded(comment);
        }, new ErrorCallback(this));

    }

    private void commentAdded(Comment comment) {

    }

    @Override
    public void onLikeComment(long id) {

    }

    @Override
    public void onUnlikeComment(long id) {
    }
    //Color tint

    /**
     * Asynchronously extract the most vibrant color from the PhotoView. Once extracted,
     * apply this tint to {@link }. This operation takes about 20-30ms
     * on a Nexus 5.
     */
    public void extractAndApplyTintFromPhotoViewAsynchronously(final Bitmap bitmap) {
        if (mMaterialColorMapUtils == null) {
            mMaterialColorMapUtils = new MaterialColorMapUtils(getResources());
        }
        if (mShotFragment == null) {
            return;
        }
        final Drawable imageViewDrawable = mShotFragment.getShotDrawable();
        new AsyncTask<Void, Void, MaterialPalette>() {
            @Override
            protected MaterialPalette doInBackground(Void... params) {


                //                if (imageViewDrawable instanceof GlideBitmapDrawable /*&& mContactData.getThumbnailPhotoBinaryData() != null && mContactData.getThumbnailPhotoBinaryData().length > 0*/) {
                // Perform the color analysis on the thumbnail instead of the full sized
                // image, so that our results will be as similar as possible to the Bugle
                // app.
                //                    final Bitmap bitmap = BitmapFactory.decodeByteArray(mContactData.getThumbnailPhotoBinaryData(), 0, mContactData.getThumbnailPhotoBinaryData().length);
                //                    final Bitmap bitmap = ((GlideBitmapDrawable)imageViewDrawable).getBitmap();
                try {
                    final int primaryColor = colorFromBitmap(bitmap);
                    if (primaryColor != 0) {
                        return mMaterialColorMapUtils.calculatePrimaryAndSecondaryColor(primaryColor);
                    }
                } finally {
                    //                        bitmap.recycle();
                }
                //                }
                //                if (imageViewDrawable instanceof LetterTileDrawable) {
                //                    final int primaryColor = ((LetterTileDrawable) imageViewDrawable).getColor();
                //                    return mMaterialColorMapUtils.calculatePrimaryAndSecondaryColor(primaryColor);
                //                }
                return MaterialColorMapUtils.getDefaultPrimaryAndSecondaryColors(getResources());
            }

            @Override
            protected void onPostExecute(MaterialPalette palette) {
                super.onPostExecute(palette);
                if (mHasComputedThemeColor) {
                    // If we had previously computed a theme color from the contact photo,
                    // then do not update the theme color. Changing the theme color several
                    // seconds after QC has started, as a result of an updated/upgraded photo,
                    // is a jarring experience. On the other hand, changing the theme color after
                    // a rotation or onNewIntent() is perfectly fine.
                    return;
                }
                // Check that the Photo has not changed. If it has changed, the new tint
                // color needs to be extracted
                //                if (imageViewDrawable == mShotFragment.getShotDrawable()) {
                mHasComputedThemeColor = true;
                setThemeColor(palette);
                //                }
            }
        }.execute();
    }

    /**
     * Examine how many white pixels are in the bitmap in order to determine whether or not
     * we need gradient overlays on top of the image.
     */
    private void analyzeWhitenessOfPhotoAsynchronously() {
        final Drawable imageViewDrawable = mShotFragment.getShotDrawable();
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if (imageViewDrawable instanceof BitmapDrawable) {
                    final Bitmap bitmap = ((BitmapDrawable) imageViewDrawable).getBitmap();
                    return WhitenessUtils.isBitmapWhiteAtTopOrBottom(bitmap);
                }
                return false;
                //                return !(imageViewDrawable instanceof LetterTileDrawable);
            }

            @Override
            protected void onPostExecute(Boolean isWhite) {
                super.onPostExecute(isWhite);
                //                mScroller.setUseGradient(isWhite);
            }
        }.execute();
    }

    private void setThemeColor(MaterialPalette palette) {
        // If the color is invalid, use the predefined default
        final int primaryColor = palette.mPrimaryColor;
        //        mScroller.setHeaderTintColor(primaryColor);
        mStatusBarColor = palette.mSecondaryColor;
        updateStatusBarColor();

        mColorFilter = new PorterDuffColorFilter(primaryColor, PorterDuff.Mode.SRC_ATOP);
        //        mContactCard.setColorAndFilter(primaryColor, mColorFilter);
        //        mRecentCard.setColorAndFilter(primaryColor, mColorFilter);
        //        mAboutCard.setColorAndFilter(primaryColor, mColorFilter);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateStatusBarColor() {
        if (mShotFragment == null || true) {
            return;
        }
        final int desiredStatusBarColor;
        // Only use a custom status bar color if QuickContacts touches the top of the viewport.
        if (/*mScroller.getScrollNeededToBeFullScreen() <= 0*/ true) {
            desiredStatusBarColor = mStatusBarColor;
        } else {
            desiredStatusBarColor = Color.TRANSPARENT;
        }
        // Animate to the new color.
        if (UiUtils.hasLollipop()) {
            final ObjectAnimator animation = ObjectAnimator.ofInt(getWindow(), "statusBarColor", getWindow().getStatusBarColor(), desiredStatusBarColor);
            animation.setDuration(ANIMATION_STATUS_BAR_COLOR_CHANGE_DURATION);
            animation.setEvaluator(new ArgbEvaluator());
            animation.start();
        }
    }

    private int colorFromBitmap(Bitmap bitmap) {
        // Author of Palette recommends using 24 colors when analyzing profile photos.
        final int NUMBER_OF_PALETTE_COLORS = 24;
        final Palette palette = Palette.generate(bitmap, NUMBER_OF_PALETTE_COLORS);
        if (palette != null && palette.getVibrantSwatch() != null) {
            return palette.getVibrantSwatch().getRgb();
        }
        return 0;
    }


    @Optional
    @InjectView(R.id.loading)
    ProgressView mProgressView;

    private void startLoadingView() {
        if (mProgressView != null) {
            mProgressView.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressView != null) {
                    mProgressView.setVisibility(View.GONE);
                }
            }
        });

    }
}
