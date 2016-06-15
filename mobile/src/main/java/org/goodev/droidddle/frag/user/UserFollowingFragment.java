package org.goodev.droidddle.frag.user;

import android.os.Bundle;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Following;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.widget.BaseAdapter;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by goodev on 2014/12/30.
 */
public class UserFollowingFragment extends BaseUserFragment<Following> {

    private UserFollowingAdapter mAdatper;

    public UserFollowingFragment() {
    }

    public static UserFollowingFragment newInstance(User user) {
        return BaseUserFragment.newInstance(UserFollowingFragment.class, user);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdatper = new UserFollowingAdapter(getActivity());
    }

    public void loadData() {
        Observable<List<Following>> observable = ApiFactory.getService(getActivity()).getUserFollowings(mUser.id, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdatper;
    }

}