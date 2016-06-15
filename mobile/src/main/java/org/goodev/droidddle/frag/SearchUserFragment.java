package org.goodev.droidddle.frag;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yfcheng on 2015/9/28.
 */
public class SearchUserFragment extends BaseRecyclerFragment<User> {
    private UserAdapter mAdatper;
    private String mQuery;
    private String mType;

    public SearchUserFragment() {
    }


    protected DividerItemDecoration getDivider() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(UiUtils.ARG_QUERY);
            mType = savedInstanceState.getString(UiUtils.ARG_TYPE);
        }
        mAdatper = new UserAdapter(getActivity(), true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        ButterKnife.inject(this, view);
        mEmpty.setText(R.string.default_empty_text);
        return view;
    }

    public void loadData() {
        Observable<List<User>> observable = Observable.create(new Observable.OnSubscribe<List<User>>() {
            @Override
            public void call(Subscriber<? super List<User>> subscriber) {
                if (TextUtils.isEmpty(mQuery)) {
                    subscriber.onNext(new ArrayList<User>());
                } else {
                    List<User> shotList = null;
                    shotList = ApiFactory.searchUser(null, mQuery, mCurrentPage);
                    subscriber.onNext(shotList);
                }
                subscriber.onCompleted();
            }
        });
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdatper;
    }


    public void updateSearch(String s) {
        mQuery = s;
        mCurrentPage = 1;
        loadFirstPageData();
    }

    @Override
    public void loadFirstPageData() {
        if (TextUtils.isEmpty(mQuery) || !isAdded()) {
            return;
        }
        super.loadFirstPageData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(UiUtils.ARG_QUERY, mQuery);
        outState.putString(UiUtils.ARG_TYPE, mType);
    }


    @Override
    protected int getColumnCount() {
        return getResources().getInteger(R.integer.shot_column);
    }

    // 搜索用户每页返回10条数据
    public boolean hasNextPage(List<User> data) {
        return data != null && data.size() >= 10;
    }
}
