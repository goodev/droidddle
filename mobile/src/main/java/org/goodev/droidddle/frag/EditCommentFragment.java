package org.goodev.droidddle.frag;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Comment;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditCommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditCommentFragment extends StatFragment {
    EditText mCommentEdit;
    private long mShotId;
    private Comment mComment;
    private MenuItem mSendMenu;

    public EditCommentFragment() {
        // Required empty public constructor
    }

    public static EditCommentFragment newInstance(long id, Comment comment) {
        EditCommentFragment fragment = new EditCommentFragment();
        Bundle args = new Bundle();
        args.putLong(UiUtils.ARG_SHOT_ID, id);
        args.putParcelable(UiUtils.ARG_COMMENT, comment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mShotId = getArguments().getLong(UiUtils.ARG_SHOT_ID);
            mComment = getArguments().getParcelable(UiUtils.ARG_COMMENT);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_comment, menu);
        mSendMenu = menu.findItem(R.id.action_send);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                postComment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void postComment() {
        String text = mCommentEdit.getText().toString();
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<Comment> observable = ApiFactory.getService(getActivity()).editShotComments(mShotId, mComment.id, text);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((comment) -> {
                    commentUpdated(comment);
                }, new ErrorCallback(getActivity()));
    }

    private void commentUpdated(Comment comment) {
        UiUtils.showToast(getActivity(), R.string.comment_updated);
        getActivity().finish();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_comment, container, false);
        mCommentEdit = (EditText) view.findViewById(R.id.shot_comment_edit);
        mCommentEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSendMenuStatus(s.toString().trim());
            }
        });
        mCommentEdit.setText(UiUtils.removePTag(mComment.body));
        return view;
    }

    private void updateSendMenuStatus(String text) {
        if (mSendMenu != null)
            mSendMenu.setEnabled(!TextUtils.isEmpty(text));

    }


}
