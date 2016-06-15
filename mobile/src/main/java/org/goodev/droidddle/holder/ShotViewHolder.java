package org.goodev.droidddle.holder;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ApiService;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.api.SucessCallback;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.FrescoUtils;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.GoURLSpan;
import org.goodev.droidddle.widget.OnOperationListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import retrofit.client.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ADMIN on 2015/1/1.
 */
public class ShotViewHolder extends RecyclerView.ViewHolder {
    @Optional
    @InjectView(R.id.user_image)
    SimpleDraweeView mUserImageView;

    @Optional
    @InjectView(R.id.user_name)
    TextView mUserNameView;

    @InjectView(R.id.shot_title)
    TextView mShotTitleView;

    @Optional
    @InjectView(R.id.shot_date)
    TextView mShotDateView;
    @InjectView(R.id.shot_views)
    TextView mShotViewsView;
    @InjectView(R.id.shot_description)
    TextView mShotDescriptionView;
    @InjectView(R.id.shot_image)
    SimpleDraweeView mShotImageView;
    @InjectView(R.id.shot_like)
    ToggleButton mShotLikeView;
    @InjectView(R.id.shot_comment)
    TextView mShotCommentView;
    @Optional
    @InjectView(R.id.shot_share)
    ImageButton mShotShareView;
    @Optional
    @InjectView(R.id.more_menu)
    ImageButton mMoreMenuView;
    @InjectView(R.id.shot_gif)
    TextView mShotGifView;

    private Activity mContext;
    private boolean mIsSelf;
    private boolean mIsBucket;
    private boolean mIsSelfLike;
    private User mUser;
    private OnUnlikeShotListener mUnlikeShotListener;
    private OnOperationListener<Shot> mOperationListener;
    private boolean mIsCleanerMode;

    public ShotViewHolder(View view, Activity context) {
        super(view);
        mContext = context;
        ButterKnife.inject(this, view);

        FrescoUtils.setShotHierarchy(mContext, mShotImageView);
    }

    public ShotViewHolder(View view, Activity context, boolean isSelf, User user) {
        super(view);
        mContext = context;
        mIsSelf = isSelf;
        mUser = user;
        ButterKnife.inject(this, view);

        FrescoUtils.setShotHierarchy(mContext, mShotImageView);
    }

    public ShotViewHolder(View view, Activity context, boolean isSelfLike, OnUnlikeShotListener listener) {
        super(view);
        mContext = context;
        mIsSelfLike = isSelfLike;
        mUnlikeShotListener = listener;
        ButterKnife.inject(this, view);

        FrescoUtils.setShotHierarchy(mContext, mShotImageView);
    }

    public void setIsBucket(boolean isBucket) {
        mIsBucket = isBucket;
    }

    public void setIsSelf(boolean isSelf) {
        mIsSelf = isSelf;
    }

    public void setOperationListener(OnOperationListener<Shot> listener) {
        mOperationListener = listener;
    }

    public void setData(Shot shot) {
        setData(shot, false, -1);
    }

    public void setData(Shot shot, int position) {
        setData(shot, false, position);
    }

    //TODO use another method to set  search  position param
    public void setData(Shot shot, boolean search) {
        setData(shot, search, -1);
    }

    public SimpleDraweeView getShotView() {
        return mShotImageView;
    }

    public void setData(Shot shot, boolean search, int position) {
        final boolean isMu = shot.id == Utils.MATERIAL_UP_ID;
        FrescoUtils.setShotUrl(mShotImageView, shot.images.normal, shot.images.teaser);

        //        Glide.with(mContext).load(shot.images.normal).placeholder(R.drawable.placeholder).into(mShotImageView);
        if (UiUtils.isGif(shot.images)) {
            mShotGifView.setVisibility(View.VISIBLE);
        } else {
            mShotGifView.setVisibility(View.INVISIBLE);
        }

        if (mMoreMenuView != null) {
            int visible = View.GONE;
            if (mIsSelf) {
                visible = View.VISIBLE;
                int menuRes = mIsBucket ? R.menu.menu_remove : R.menu.menu_edit_delete;
                UiUtils.setupOverflowEditMenu(mContext, mMoreMenuView, mOperationListener, menuRes, shot, position);
            }
            mMoreMenuView.setVisibility(visible);
        }
        mShotLikeView.setChecked(mIsSelfLike);
        final User user = shot.user;
        if (user != null && mUserImageView != null) {
            mUserNameView.setText(user.name);
            mUserImageView.setImageURI(Uri.parse(user.avatarUrl));
            //            Glide.with(mContext).load(user.avatarUrl).placeholder(R.drawable.person_image_empty).into(mUserImageView);
            View.OnClickListener userListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (search) {
                        UiUtils.launchUser(mContext, user.username, mUserImageView);
                    } else {
                        UiUtils.launchUser(mContext, user, mUserImageView);
                    }
                }
            };
            if (!isMu) {
                mUserImageView.setOnClickListener(userListener);
            }
        }
        if (true || !mIsSelf) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isMu) {
                        Utils.openPlayStore(mContext, Utils.MATERIAL_UP);
                        return;
                    }
                    if (shot.user == null) {
                        shot.user = mUser;
                    }
                    if (search) {
                        UiUtils.launchShot(mContext, shot.id);
                    } else {
                        UiUtils.launchShot(mContext, shot, mShotImageView);
                    }
                }
            });
        }

        if (mIsCleanerMode) {
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    if (!TextUtils.isEmpty(shot.description)) {
//                        mShotDescriptionView.setVisibility(View.VISIBLE);
//                    }
//                    if (mUserNameView != null) {
//                        mUserNameView.setVisibility(View.VISIBLE);
//                    }
//                    return true;
//                }
//            });
//            itemView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    switch (event.getActionMasked()) {
//                        case MotionEvent.ACTION_CANCEL:
//                        case MotionEvent.ACTION_UP:
//                        case MotionEvent.ACTION_OUTSIDE:
//                            mShotDescriptionView.setVisibility(View.INVISIBLE);
//                            if (mUserNameView != null) {
//                                mUserNameView.setVisibility(View.INVISIBLE);
//                            }
//                    }
//
//                    return false;
//                }
//            });
        }

        String comments = String.valueOf(shot.commentsCount.intValue());
        mShotCommentView.setText(comments);
        //        mShotCommentView.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                if (search) {
        //                    UiUtils.launchShot(mContext, shot.id);
        //                } else {
        //                    UiUtils.launchShot((android.app.Activity) mContext, shot, true);
        //                }
        //            }
        //        });
        String likes = String.valueOf(shot.likesCount.intValue());
        mShotLikeView.setTextOff(likes);
        mShotLikeView.setTextOn(likes);
        mShotLikeView.setText(likes);
        //        mShotLikeView.setChecked(shot.);
        mShotLikeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!Utils.hasInternet(mContext)) {
                    Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isMu) {
                    Utils.openPlayStore(mContext, Utils.MATERIAL_UP);
                    return;
                }
                ApiService service = ApiFactory.getService(mContext);
                Observable<Response> observable;
                int res;
                ToggleButton toggleButton = (ToggleButton) v;
                if (toggleButton.isChecked()) {
                    observable = service.likeShot(shot.id);
                    res = R.string.like_shot_success;
                    Animation likeAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.like_btn_anim);
                    Animation scaleAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.like_scale_anim);
                    AnimationSet animSet = new AnimationSet(false);
                    animSet.addAnimation(likeAnim);
                    animSet.addAnimation(scaleAnim);

                    v.startAnimation(scaleAnim);
                } else {
                    res = R.string.unlike_shot_success;
                    observable = service.unlikeShot(shot.id);
                    if (position != -1 && mUnlikeShotListener != null) {
                        mUnlikeShotListener.onUnlikeShot(position);
                    }
                }
                observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SucessCallback<Response>(mContext, res), new ErrorCallback(mContext));
            }
        });

        mShotTitleView.setText(shot.title);
        mShotViewsView.setText(String.valueOf(shot.viewsCount));
        //        CharSequence date = DateUtils.getRelativeDateTimeString(mContext,shot.createdAt.getTime(),DateUtils.DAY_IN_MILLIS,DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE|DateUtils.FORMAT_NUMERIC_DATE);
        //        CharSequence date = DateUtils.formatDateTime(mContext,shot.createdAt.getTime(),DateUtils.FORMAT_NUMERIC_DATE);
        if (mShotDateView != null) {
            if (shot.createdAt != null) {
                CharSequence date = DateUtils.getRelativeTimeSpanString(shot.createdAt.getTime(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE | DateUtils.FORMAT_NUMERIC_DATE);
                mShotDateView.setText(date);
            } else {
                mShotDateView.setText(null);
            }
        }
        if (TextUtils.isEmpty(shot.description)) {
            mShotDescriptionView.setText(null);
            mShotDescriptionView.setVisibility(View.INVISIBLE);
        } else {
            mShotDescriptionView.setVisibility(mIsCleanerMode ? View.INVISIBLE : View.VISIBLE);
            Spanned spannable = GoURLSpan.hackURLSpan(Html.fromHtml(shot.description));
            mShotDescriptionView.setText(spannable);
            mShotDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
            //            mShotDescriptionView.requestLayout();
        }

        if (mShotShareView != null && !isMu) {
            mShotShareView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UiUtils.shareShot(mContext, shot);
                }
            });
        }
    }

    public void setCleanerMode(boolean cleanerMode) {
        mIsCleanerMode = cleanerMode;
    }
}
