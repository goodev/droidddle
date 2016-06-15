package org.goodev.droidddle.frag.user;

import android.os.Bundle;

import org.goodev.droidddle.BuildConfig;
import org.goodev.droidddle.MainActivity;
import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.frag.ShotsAdapter;
import org.goodev.droidddle.notif.FollowingCheckService;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ADMIN on 2015/5/15.
 */
public class UserFollowingShotFragment extends BaseUserFragment<Shot> {

    private ShotsAdapter mAdatper;

    public UserFollowingShotFragment() {
    }

    public static UserFollowingShotFragment newInstance(User user) {
        return BaseUserFragment.newInstance(UserFollowingShotFragment.class, user);
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
        mAdatper = new ShotsAdapter(getActivity());
    }

    // 第一页数据的第一个 更新 通知的id
    public void loadData() {
        Observable<List<Shot>> observable = ApiFactory.getService(getActivity())
                .getUserFollowingShots(mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((data) -> {
                    updateData(data);
                }, new ErrorCallback(getActivity()));
    }

    @Override
    public void updateData(List<Shot> data) {
        if (mCurrentPage == 1 && data.size() > 0) {
            if (!BuildConfig.DEBUG) {
                FollowingCheckService.getSharedPreferences(getActivity())
                        .edit().putLong(FollowingCheckService.PREF_LAST_SHOT_ID, data.get(0).id).commit();
            }
        }
        super.updateData(data);
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdatper;
    }
}
