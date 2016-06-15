package org.goodev.droidddle.frag.user;

import android.os.Bundle;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Project;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.widget.BaseAdapter;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by goodev on 2014/12/30.
 */
public class UserProjectFragment extends BaseUserFragment<Project> {

    private UserProjectAdapter mAdatper;

    public UserProjectFragment() {
    }

    public static UserProjectFragment newInstance(User user) {
        return BaseUserFragment.newInstance(UserProjectFragment.class, user);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdatper = new UserProjectAdapter(getActivity());
    }

    public void loadData() {
        Observable<List<Project>> observable = ApiFactory.getService(getActivity()).getUserProjects(mUser.id, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdatper;
    }

}
