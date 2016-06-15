package org.goodev.droidddle.frag.user;

import android.os.Bundle;

import org.goodev.droidddle.MainActivity;
import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.LikedShot;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by goodev on 2014/12/30.
 */
public class UserLikedShotFragment extends BaseUserFragment<LikedShot> {

    private UserLikedShotAdapter mAdatper;

    public UserLikedShotFragment() {
    }

    public static UserLikedShotFragment newInstance(User user) {
        return BaseUserFragment.newInstance(UserLikedShotFragment.class, user);
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
        mAdatper = new UserLikedShotAdapter(getActivity(), OAuthUtils.isSelf(mUser.id));
    }


    public void loadData() {
        Observable<List<LikedShot>> observable = ApiFactory.getService(getActivity()).getUserLikedShot(mUser.id, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdatper;
    }
}
