package org.goodev.droidddle.frag.shot;


import android.app.Fragment;
import android.os.Bundle;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Attachment;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShotAttachmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShotAttachmentFragment extends BaseShotFragment<Attachment> {
    ShotAttachmentAdatper mAdapter;
    private boolean mIsSelf;

    public ShotAttachmentFragment() {
        // Required empty public constructor
    }

    public static ShotAttachmentFragment newInstance(long id, boolean isSelf) {
        ShotAttachmentFragment fragment = new ShotAttachmentFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SHOT_ID, id);
        args.putBoolean(UiUtils.ARG_SELF, isSelf);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsSelf = getArguments().getBoolean(UiUtils.ARG_SELF);
        mAdapter = new ShotAttachmentAdatper(getActivity(), mShotId, mIsSelf);
    }

    public void loadData() {
        Observable<List<Attachment>> observable = ApiFactory.getService(getActivity()).getShotAttachments(mShotId, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    protected DividerItemDecoration getDivider() {
        return null;
    }

}
