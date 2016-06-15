package org.goodev.droidddle.frag.shot;


import android.app.Fragment;
import android.os.Bundle;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Like;
import org.goodev.droidddle.widget.BaseAdapter;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShotLikeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShotLikeFragment extends BaseShotFragment<Like> {
    private static final String ARG_PARAM2 = "param2";
    ShotLikeAdapter mAdapter;
    private String mParam2;

    public ShotLikeFragment() {
        // Required empty public constructor
    }

    public static ShotLikeFragment newInstance(long id, String param2) {
        ShotLikeFragment fragment = new ShotLikeFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SHOT_ID, id);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mAdapter = new ShotLikeAdapter(getActivity());
    }

    public void loadData() {
        Observable<List<Like>> observable = ApiFactory.getService(getActivity()).getShotLikes(mShotId, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdapter;
    }


}
