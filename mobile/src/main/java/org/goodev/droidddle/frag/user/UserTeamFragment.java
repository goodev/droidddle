package org.goodev.droidddle.frag.user;

import android.os.Bundle;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Team;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by goodev on 2014/12/30.
 */
public class UserTeamFragment extends BaseUserFragment<Team> {

    private UserTeamAdapter mAdatper;

    public UserTeamFragment() {
    }

    public static UserTeamFragment newInstance(User user) {
        return BaseUserFragment.newInstance(UserTeamFragment.class, user);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdatper = new UserTeamAdapter(getActivity());
    }

    protected DividerItemDecoration getDivider() {
        return null;
    }

    public void loadData() {
        Observable<List<Team>> observable = ApiFactory.getService(getActivity()).getUserTeams(mUser.id, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdatper;
    }

}
