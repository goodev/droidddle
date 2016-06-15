package org.goodev.droidddle.frag.shot;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;
import org.goodev.droidddle.widget.GridSpanSizeLookup;
import org.goodev.droidddle.widget.OverScrollRecyclerView;
import org.goodev.droidddle.widget.ProgressView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ADMIN on 2015/1/1.
 */
public abstract class BaseShotFragment<T extends Parcelable> extends ObservableFragment {

    @InjectView(R.id.recycler_view)
    OverScrollRecyclerView mRecyclerView;
    @InjectView(R.id.empty)
    TextView mEmpty;
    @InjectView(R.id.loading)
    ProgressView mLoading;

    ArrayList<T> mDataList = new ArrayList<>();

    public BaseShotFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(UiUtils.ARG_CURRENT_PAGE, 1);
            ArrayList<T> datas = savedInstanceState.getParcelableArrayList(UiUtils.ARG_DATA_LIST);
            if (datas != null) {
                mDataList.addAll(datas);
            }
        }
    }

    @Override
    public OverScrollRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shot_base, container, false);

        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OverScrollRecyclerView mRecyclerView = getRecyclerView();
        BaseAdapter mAdapter = getAdapter();

        if (mOverListener != null) {
            mRecyclerView.setOnOverScrollListener(mOverListener);
        }

        int column = getColumnCount();
        RecyclerView.LayoutManager layoutManager;
        if (column >= 2) {
            GridLayoutManager sglManager = new GridLayoutManager(getActivity(), column, GridLayoutManager.VERTICAL, false);
            layoutManager = sglManager;
            sglManager.setSpanSizeLookup(new GridSpanSizeLookup(mAdapter, column));
        } else {
            LinearLayoutManager llManager = new LinearLayoutManager(getActivity());
            llManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager = llManager;
        }

        getRecyclerView().setLayoutManager(layoutManager);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setEmptyView(mEmpty);
        mRecyclerView.setLoadingView(mLoading);
        mRecyclerView.setOnLoadingMoreListener(this);
        DividerItemDecoration divider = getDivider();
        if (divider != null) {
            mRecyclerView.addItemDecoration(divider);
        } else {
            int p = (int) getResources().getDimension(R.dimen.keyline_1_minus_12dp);
            mRecyclerView.setPadding(p, p, p, p);
        }
        if (savedInstanceState == null || mDataList.isEmpty()) {
            loadFirstPageData();
        } else {
            mAdapter.setData(mDataList);
        }

    }

    protected DividerItemDecoration getDivider() {
        return UiUtils.getDividerItemDecoration(getActivity().getResources());
    }


    public void loadFirstPageData() {
        mRecyclerView.setLoading(true);
        mCurrentPage = 1;
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        loadData();
    }

    public void updateData(List<T> data) {
        mRecyclerView.setLoading(false);
        BaseAdapter mAdapter = getAdapter();
        mAdapter.setLoading(false);
        mRecyclerView.finishLoadingMore(ApiFactory.hasNextPage(data));
        if (mCurrentPage == 1) {
            mDataList.clear();
            mAdapter.setData(data);
        } else {
            mAdapter.addData(data);
        }

        mDataList.addAll(data);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(UiUtils.ARG_CURRENT_PAGE, mCurrentPage);
        outState.putParcelableArrayList(UiUtils.ARG_DATA_LIST, mDataList);
    }

    @Override
    public void onLoadingMore() {
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        getAdapter().setLoading(true);
        mCurrentPage++;
        loadData();
    }

    @Override
    public void isFirstItemFullVisible(boolean firstVisible) {

    }
}
