package org.goodev.droidddle.frag.team;

import android.os.Bundle;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Team;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.widget.BaseAdapter;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by goodev on 2014/12/30.
 */
public class TeamMemberFragment extends BaseTeamFragment<User> {

    private TeamMemberAdapter mAdatper;

    public TeamMemberFragment() {
    }

    public static TeamMemberFragment newInstance(Team user) {
        return BaseTeamFragment.newInstance(TeamMemberFragment.class, user);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdatper = new TeamMemberAdapter(getActivity());
    }

    public void loadData() {
        Observable<List<User>> observable = ApiFactory.getService(getActivity()).getTeamMembers(mUser.id, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdatper;
    }


}
