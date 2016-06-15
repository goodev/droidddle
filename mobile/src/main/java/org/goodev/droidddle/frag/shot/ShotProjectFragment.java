package org.goodev.droidddle.frag.shot;


import android.app.Fragment;
import android.os.Bundle;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Project;
import org.goodev.droidddle.widget.BaseAdapter;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShotProjectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShotProjectFragment extends BaseShotFragment<Project> {
    ShotProjectAdapter mAdapter;

    public ShotProjectFragment() {
        // Required empty public constructor
    }

    public static ShotProjectFragment newInstance(long id, long count) {
        ShotProjectFragment fragment = new ShotProjectFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SHOT_ID, id);
        args.putLong(ARG_COUNT, count);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ShotProjectAdapter(getActivity());
    }


    public void loadData() {
        Observable<List<Project>> observable = ApiFactory.getService(getActivity()).getShotProjects(mShotId, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdapter;
    }

}
