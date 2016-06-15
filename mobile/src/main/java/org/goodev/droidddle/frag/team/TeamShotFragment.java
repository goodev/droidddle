package org.goodev.droidddle.frag.team;

import android.os.Bundle;

import org.goodev.droidddle.MainActivity;
import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.pojo.Team;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by goodev on 2014/12/30.
 */
public class TeamShotFragment extends BaseTeamFragment<Shot> {

    private TeamShotAdapter mAdatper;

    public TeamShotFragment() {
    }

    public static TeamShotFragment newInstance(Team user) {
        return BaseTeamFragment.newInstance(TeamShotFragment.class, user);
    }

    @Override
    protected int getColumnCount() {
        if (getActivity() instanceof MainActivity) {
            return getResources().getInteger(R.integer.shot_column);
        }
        return getResources().getInteger(R.integer.user_shot_column);
    }

    protected DividerItemDecoration getDivider() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdatper = new TeamShotAdapter(getActivity());
    }

    public void loadData() {
        Observable<List<Shot>> observable = ApiFactory.getService(getActivity()).getTeamShots(mUser.id, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdatper;
    }

}
