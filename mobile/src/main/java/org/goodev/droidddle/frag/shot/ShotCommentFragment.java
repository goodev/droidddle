package org.goodev.droidddle.frag.shot;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Comment;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.ShotPref;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.OverScrollRecyclerView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link org.goodev.droidddle.frag.shot.ShotCommentFragment.OnCommentActionListener} interface
 * to handle interaction events.
 * Use the {@link ShotCommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShotCommentFragment extends BaseShotFragment<Comment> {
    public static final String ARG_COLOR = "extra_color";
    ShotCommentAdapter mAdapter;
    @InjectView(R.id.shot_comment_edit)
    EditText mCommentEditView;
    @InjectView(R.id.shot_comment_send)
    ImageButton mSendButton;

    private int mColor;
    private OnCommentActionListener mListener;
    private ProgressDialog mDialog;
    private Observable<Comment> mPostCommentObservable;

    public ShotCommentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param id    Parameter 1.
     * @param color Parameter 2.
     * @return A new instance of fragment ShotCommentFragment.
     */
    public static ShotCommentFragment newInstance(long id, int color) {
        ShotCommentFragment fragment = new ShotCommentFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SHOT_ID, id);
        args.putInt(ARG_COLOR, color);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColor = getArguments().getInt(ARG_COLOR);
        }
        mAdapter = new ShotCommentAdapter(getActivity(), mShotId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shot_comment, container, false);
        ButterKnife.inject(this, view);

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

        String comment = ShotPref.getComment(mShotId);
        if (!TextUtils.isEmpty(comment)) {
            mCommentEditView.setText(comment);
        } else {
            mSendButton.setEnabled(false);
        }
        return view;
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

    public void loadData() {
        Observable<List<Comment>> commentObservable = ApiFactory.getService(getActivity()).getShotComments(mShotId, mCurrentPage);
        commentObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((comments) -> {
            updateData(comments);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public OverScrollRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    //    public void onPostComment(String text) {
    //        if (mListener != null) {
    //            mListener.onPostComment(text);
    //        }
    //    }

//    private void updateData(List<Comment> comments) {
//        L.d("comments = [" + "]" + (comments.isEmpty()) + " ");
//        mAdapter.setLoading(false);
//        mRecyclerView.finishLoadingMore(ApiFactory.hasNextPage(comments));
//        mAdapter.addData(comments);
//    }

    @OnClick(R.id.shot_comment_send)
    void onSendButtonClicked(View view) {
        String text = mCommentEditView.getText().toString().trim();
        onPostComment(text);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String text = mCommentEditView.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            ShotPref.saveComment(mShotId, text);
        }

    }

    public void onPostComment(String text) {
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        String message = getString(R.string.sending);
        mDialog = UiUtils.showProgressDialog(getActivity(), message);

        ShotPref.saveComment(mShotId, text);
        if (Pref.getSendByComment(getActivity())) {
            text += getString(R.string.send_by_text);
        }
        mPostCommentObservable = ApiFactory.getService(getActivity()).postShotComments(mShotId, text);
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
        ShotPref.removeComment(mShotId);
        UiUtils.dismissDialog(mDialog);
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        mAdapter.addData(comment);
        mCommentEditView.setText(null);
        Toast.makeText(getActivity(), R.string.comment_added, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCommentActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCommentActionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * save comment text to shot_comment_idxxx
     * if upload failed , reupload on the background
     * <p>
     * Post a comment on Shot or (un)like a comment
     */
    public interface OnCommentActionListener {
        public void onPostComment(String text);

        public void onLikeComment(long id);

        public void onUnlikeComment(long id);
    }

}
