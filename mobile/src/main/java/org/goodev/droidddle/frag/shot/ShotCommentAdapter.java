package org.goodev.droidddle.frag.shot;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.api.SucessCallback;
import org.goodev.droidddle.pojo.Comment;
import org.goodev.droidddle.pojo.Like;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.GoURLSpan;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.client.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by goodev on 2014/12/26.
 */
public class ShotCommentAdapter extends BaseAdapter<Comment> {

    private long mShotId;

    public ShotCommentAdapter(Activity context, long id) {
        super(context);
        mShotId = id;
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }


    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.shot_comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Comment comment = getItem(position);
        CommentViewHolder h = (CommentViewHolder) holder;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelf = OAuthUtils.isSelf(comment.user.id);
                boolean showWhyLike = comment.likesCount > 0 && Pref.isFirstLikeAndUnlike(mContext);
                String[] items = UiUtils.getCommentMenu(mContext, isSelf, comment.likesCount, showWhyLike);
                CommentCallback callback = new CommentCallback(mContext, mShotId, comment, isSelf, showWhyLike, items.length);
                UiUtils.showCommentItemMenu(mContext, items, callback);
            }
        };

        h.mUserImageView.setImageURI(Uri.parse(comment.user.avatarUrl));
//        Glide.with(mContext).load(comment.user.avatarUrl).placeholder(R.drawable.person_image_empty).into(h.mUserImageView);
        h.mUserImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.launchUser((android.app.Activity) mContext, comment.user);
            }
        });
        if (TextUtils.isEmpty(comment.body)) {
            h.mBodyView.setText(comment.body);
            h.mBodyView.setOnClickListener(listener);
        } else {
            String body = UiUtils.removePTag(comment.body);
            Spanned bodyValue = Html.fromHtml(body);
            boolean hasUrl = GoURLSpan.hackURLSpanHasResult((SpannableStringBuilder) bodyValue);
            if (hasUrl) {
                h.mBodyView.setLinksClickable(true);
                h.mBodyView.setClickable(false);
                h.mBodyView.setOnClickListener(null);
                h.mBodyView.setText(bodyValue);
                h.mBodyView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                h.mBodyView.setLinksClickable(false);
                h.mBodyView.setOnClickListener(listener);
                h.mBodyView.setText(bodyValue);
            }
        }
        CharSequence date = DateUtils.getRelativeTimeSpanString(comment.createdAt.getTime(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE | DateUtils.FORMAT_NUMERIC_DATE);
        h.mDateView.setText(date);
        h.mUserNameView.setText(comment.user.name);
        String counts = String.valueOf(comment.likesCount);
        h.mCountsView.setText(counts);


        h.itemView.setOnClickListener(listener);


    }

    public static class CommentCallback implements DialogInterface.OnClickListener {
        Context mContext;
        Comment mComment;
        boolean mIsSelf;
        boolean mShowWhyLike;
        int mSize;
        long mShotId;

        public CommentCallback(Context context, long id, Comment comment, boolean isSelf, boolean showWhyLike, int size) {
            mContext = context;
            mShotId = id;
            mComment = comment;
            mIsSelf = isSelf;
            mShowWhyLike = showWhyLike;
            mSize = size;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    copyToClipboard();
                    UiUtils.showToast(mContext, R.string.text_copied_to_clipboard);
                    break;
                case 1:
                    if (mIsSelf) {
                        // Edit
                        editComment();
                    } else {
                        likeComment();
                    }
                    break;
                case 2:
                    if (mIsSelf) {
                        deleteComment();
                    } else {
                        unlikeComment();
                    }
                    break;
                case 3:
                    if (mIsSelf) {
                        likeComment();
                    } else if (mShowWhyLike) {
                        whyLikeAndUnlikeComment();
                    } else {
                        whoLikeThis();
                    }
                    break;
                case 4:
                    if (mIsSelf) {
                        unlikeComment();
                    } else {
                        whoLikeThis();
                    }
                    break;
                case 5:
                    if (mShowWhyLike) {
                        whyLikeAndUnlikeComment();
                    } else {
                        whoLikeThis();
                    }
                    break;
                case 6:
                    whoLikeThis();
                    break;
            }

        }

        //TODO ...
        private void unlikeComment() {
            if (!Utils.hasInternet(mContext)) {
                Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
                return;
            }
            Observable<Response> observable = ApiFactory.getService(mContext).postUnlikeShotComments(mShotId, mComment.id);
            observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new SucessCallback<Response>(mContext, R.string.unlike_comment_success),
                            new ErrorCallback(mContext));
        }

        private void whyLikeAndUnlikeComment() {
            UiUtils.showWhyLikeAndUnlikeDialog(mContext);
        }

        private void whoLikeThis() {
            //TODO ..
            //            UiUtils.editComment(mContext, mShotId, mComment);
        }

        private void deleteComment() {
            if (!Utils.hasInternet(mContext)) {
                Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
                return;
            }
            Observable<Response> observable = ApiFactory.getService(mContext)
                    .deleteShotComments(mShotId, mComment.id);
            observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SucessCallback<Response>(mContext, R.string.delete_comment_success), new ErrorCallback(mContext));
        }

        private void likeComment() {
            if (!Utils.hasInternet(mContext)) {
                Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
                return;
            }
            Observable<Like> observable = ApiFactory.getService(mContext).postLikeShotComments(mShotId, mComment.id);
            observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SucessCallback<Like>(mContext, R.string.like_comment_success),
                            new ErrorCallback(mContext));
        }

        private void editComment() {
            UiUtils.editComment(mContext, mShotId, mComment);
        }

        private void copyToClipboard() {
            String text = mComment.body;
            if (text != null) {
                text = text.replaceAll("<p>|</p>", "");
            }
            UiUtils.copyToClipboard(mContext, text);
        }

    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.user_image)
        SimpleDraweeView mUserImageView;
        @InjectView(R.id.user_name)
        TextView mUserNameView;
        @InjectView(R.id.date_view)
        TextView mDateView;
        @InjectView(R.id.counts_view)
        TextView mCountsView;
        @InjectView(R.id.body_text_view)
        TextView mBodyView;

        public CommentViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
