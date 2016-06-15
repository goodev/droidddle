package org.goodev.droidddle.frag;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by goodev on 2015/1/8.
 */
public class SearchBaseFragment extends BaseRecyclerFragment<Shot> {
    private ShotsAdapter mAdatper;
    private String mQuery;
    private String mType;

    public SearchBaseFragment() {
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
        mAdatper = new ShotsAdapter(getActivity(), true);
    }

    public void loadData() {

        Observable<List<Shot>> observable = Observable.create(new Observable.OnSubscribe<List<Shot>>() {
            @Override
            public void call(Subscriber<? super List<Shot>> subscriber) {
                if (TextUtils.isEmpty(mQuery)) {
                    subscriber.onNext(new ArrayList<Shot>());
                } else {
                    List<Shot> shotList = null;
                    if (mIsColor) {
                        shotList = ApiFactory.colorShot(null, mQuery, mPercent, mCurrentPage);
                    } else if (mQuery != null && mQuery.startsWith(ApiFactory.TAG_SEARCH)) {
                        shotList = ApiFactory.tagShot(null, mQuery.substring(4), mType, mCurrentPage);
                    } else {
                        shotList = ApiFactory.searchShot(null, mQuery, mType, mCurrentPage);
                    }
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

    private boolean mIsColor;
    private int mPercent;

    public void updateSearch(String color, int percent) {
        mIsColor = true;
        mCurrentPage = 1;
        mQuery = color;
        mPercent = percent;
        loadFirstPageData();
    }

    public void updateSearch(String s) {
        if (mQuery != null && mQuery.equals(s)) {
            return;
        }
        mQuery = s;
        mCurrentPage = 1;
        loadFirstPageData();
    }

    @Override
    public void loadFirstPageData() {
        if (TextUtils.isEmpty(mQuery)) {
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

    public void updateSearchType(String type) {
        if (type.equals(mType)) {
            return;
        }
        mType = type;
        if (mType == null && type.equals(ApiFactory.POPULAR)) {
            return;
        }

        loadFirstPageData();
    }

    @Override
    protected int getColumnCount() {
        return getResources().getInteger(R.integer.shot_column);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
