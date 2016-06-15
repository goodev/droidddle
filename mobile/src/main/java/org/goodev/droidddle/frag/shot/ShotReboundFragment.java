package org.goodev.droidddle.frag.shot;


import android.app.Fragment;
import android.os.Bundle;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShotReboundFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShotReboundFragment extends BaseShotFragment<Shot> {
    ShotReboundAdapter mAdapter;

    public ShotReboundFragment() {
        // Required empty public constructor
    }

    public static ShotReboundFragment newInstance(long id, long count) {
        ShotReboundFragment fragment = new ShotReboundFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SHOT_ID, id);
        args.putLong(ARG_COUNT, count);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ShotReboundAdapter(getActivity());
    }

    public void loadData() {
        Observable<List<Shot>> observable = ApiFactory.getService(getActivity()).getShotRebounds(mShotId, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    protected int getColumnCount() {
        return getResources().getInteger(R.integer.two_pane_shot_column);
    }

    @Override
    protected DividerItemDecoration getDivider() {
        return null;
    }
}
