package org.goodev.droidddle.frag.user;

import android.app.Activity;
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

import org.goodev.droidddle.MainActivity;
import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.frag.StatFragment;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;
import org.goodev.droidddle.widget.GridSpanSizeLookup;
import org.goodev.droidddle.widget.OnLoadingMoreListener;
import org.goodev.droidddle.widget.ProgressView;
import org.goodev.droidddle.widget.QuickReturnRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ADMIN on 2014/12/29.
 */
public abstract class BaseUserFragment<T extends Parcelable> extends StatFragment implements OnLoadingMoreListener {
    public static final String ARG_INITIAL_POSITION = "arg_init_position";

    @InjectView(R.id.recycler_view)
    QuickReturnRecyclerView mRecyclerView;
    @InjectView(R.id.empty)
    TextView mEmpty;
    @InjectView(R.id.loading)
    ProgressView mLoading;
    User mUser;
    int mCurrentPage = 1;

    ArrayList<T> mDataList = new ArrayList<>();
//    FloatingActionButton mFab;

    public static <E extends BaseUserFragment> E newInstance(Class<E> clazz, User user) {
        try {
            E fragment = clazz.newInstance();
            Bundle args = new Bundle();
            args.putParcelable(UiUtils.ARG_USER, user);
            fragment.setArguments(args);
            return fragment;
        } catch (java.lang.InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public void scrollToTop() {
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = getArguments().getParcelable(UiUtils.ARG_USER);
        }
        if (mUser == null) {
            getActivity().finish();
        }
        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(UiUtils.ARG_CURRENT_PAGE, 1);
            ArrayList<T> datas = savedInstanceState.getParcelableArrayList(UiUtils.ARG_DATA_LIST);
            if (datas != null) {
                mDataList.addAll(datas);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_base, container, false);

        if (getActivity() instanceof MainActivity) {
            view.setPadding(0, 0, 0, 0);
        }
        ButterKnife.inject(this, view);
        return view;
    }

    public abstract void loadData();

    public abstract BaseAdapter getAdapter();

    public QuickReturnRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        QuickReturnRecyclerView mRecyclerView = getRecyclerView();
        BaseAdapter mAdapter = getAdapter();

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

        Activity parentActivity = getActivity();
        //        View headerView = LayoutInflater.from(parentActivity).inflate(R.layout.padding, null);
        //        recyclerView.setAdapter(new SimpleHeaderRecyclerAdapter(parentActivity, items, headerView));


//        if (mFab != null) {
//            mFab.attachToRecyclerView(mRecyclerView);
//        }
    }

    protected int getColumnCount() {
        return 1;
    }

    protected DividerItemDecoration getDivider() {
        return UiUtils.getDividerItemDecoration(getActivity().getResources());
    }

    public void loadFirstPageData() {
        mRecyclerView.setLoading(true, true);
        mCurrentPage = 1;
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        loadData();
    }

    public void updateData(List<T> data) {
        mRecyclerView.setLoading(false, mCurrentPage == 1);
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

//    public void attachFab(FloatingActionButton fab) {
//        mFab = fab;
//        if (mRecyclerView != null) {
//            fab.attachToRecyclerView(mRecyclerView);
//        }
//    }
}
