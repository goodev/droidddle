package org.goodev.droidddle.frag;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.Ads;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;
import org.goodev.droidddle.widget.OnLoadingMoreListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomeFragment extends BaseRecyclerFragment<Shot> implements OnLoadingMoreListener, SwipeRefreshLayout.OnRefreshListener {
    @InjectView(R.id.swipe_container)
    SwipeRefreshLayout mRefreshLayout;

    private String mSort;
    private String mList;
    private String mTimeframe;
    private ShotsAdapter mAdapter;

    private boolean mIsCleanerMode;

    public HomeFragment() {
    }

    public static Fragment newInstance(String list, String sort, String timeframe) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(UiUtils.ARG_SHOT_SORT, sort);
        args.putString(UiUtils.ARG_SHOT_LIST, list);
        args.putString(UiUtils.ARG_SHOT_TIMEFRAME, timeframe);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(UiUtils.ARG_SHOT_SORT, mSort);
        outState.putString(UiUtils.ARG_SHOT_LIST, mList);
        outState.putString(UiUtils.ARG_SHOT_TIMEFRAME, mTimeframe);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsCleanerMode = Pref.isCleanerMode(getActivity());
        if (savedInstanceState != null) {
            mSort = savedInstanceState.getString(UiUtils.ARG_SHOT_SORT);
            mList = savedInstanceState.getString(UiUtils.ARG_SHOT_LIST);
            mTimeframe = savedInstanceState.getString(UiUtils.ARG_SHOT_TIMEFRAME);
        } else if (getArguments() != null) {

            mSort = getArguments().getString(UiUtils.ARG_SHOT_SORT);
            mList = getArguments().getString(UiUtils.ARG_SHOT_LIST);
            mTimeframe = getArguments().getString(UiUtils.ARG_SHOT_TIMEFRAME);
        }
        mAdapter = new ShotsAdapter(getActivity());

        setHasOptionsMenu(mIsCleanerMode);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_filter, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String timeframe = null;
        switch (item.getItemId()) {
            case R.id.time_filter_now:
                timeframe = "";
                break;
            case R.id.time_filter_week:
                timeframe = "week";
                break;
            case R.id.time_filter_month:
                timeframe = "month";
                break;
            case R.id.time_filter_year:
                timeframe = "year";
                break;
            case R.id.time_filter_ever:
                timeframe = "ever";
                break;
        }
        if (timeframe != null) {
            item.setChecked(!item.isChecked());
            if (!isEquals(timeframe, mTimeframe)) {
                mTimeframe = timeframe;
                reloadData();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateData(List<Shot> shots) {
        if (mCurrentPage == 1 && TextUtils.isEmpty(mList) && shots != null && !Utils.isAppInstalled(getActivity(), Utils.MATERIAL_UP)) {
            shots.add(2, Utils.getMaterialUpPost());
        }
        if (!shots.isEmpty()) {
            Ads.addAdToShot(shots, mAdapter.getAllItems());
        }
        super.updateData(shots);
        mRefreshLayout.setRefreshing(false);
        if (shots == null) {
            return;
        }
        if (mCurrentPage == 1) {
            mRecyclerView.scrollToPosition(0);
            mRefreshLayout.setEnabled(true);
        }
    }

    public void scrollToTop() {
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(0);
            mRefreshLayout.setEnabled(true);
        }
    }

    @Override
    protected int getColumnCount() {
        return getResources().getInteger(R.integer.shot_column);
    }

    protected DividerItemDecoration getDivider() {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);
//        if (mIsCleanerMode) {
////            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mSpinnerSort.getLayoutParams();
////            params.weight = 0;
////            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
////            params = (LinearLayout.LayoutParams) mSpinnerList.getLayoutParams();
////            params.weight = 0;
////            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//            mSpinnerTimeframe.setVisibility(View.GONE);
//            mFilterPadding1.setVisibility(View.GONE);
//        } else {
//            mSpinnerTimeframe.setVisibility(View.VISIBLE);
//            mFilterPadding1.setVisibility(View.VISIBLE);
//        }
//
//        mSortEntries = res.getStringArray(R.array.shots_sort_entry);
//        mSpinnerSort.setText(mSortEntries[0]);
////        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_dropdown_item, sortTexts);
////        mSpinnerSort.setAdapter(sortAdapter);
//        mListEntries = res.getStringArray(R.array.shots_list_entry);
//        mSpinnerList.setText(mListEntries[0]);
////        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_dropdown_item, mListEntries);
////        mSpinnerList.setAdapter(listAdapter);
//        mTimeEntries = res.getStringArray(R.array.shots_timeframe_entry);
//        mSpinnerTimeframe.setText(mTimeEntries[0]);
////        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_dropdown_item, sortTimes);
////        mSpinnerTimeframe.setAdapter(timeAdapter);
//
//        if (!TextUtils.isEmpty(mSort)) {
//            mSpinnerSort.setText(mSortEntries[getSelection(mSortValues, mSort)]);
//        }
//        if (!TextUtils.isEmpty(mList)) {
//            mSpinnerList.setText(mListEntries[getSelection(mListValues, mList)]);
//        }
//        if (!TextUtils.isEmpty(mTimeframe)) {
//            mSpinnerTimeframe.setText(mTimeEntries[getSelection(mTimeframeValues, mTimeframe)]);
//        }
//
////        mSpinnerTimeframe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
////            @Override
////            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
////                onTimeframeItemSelected(position);
////            }
////
////            @Override
////            public void onNothingSelected(AdapterView<?> parent) {
////
////            }
////        });
//        UiUtils.setupFilterPopupMenu(getActivity(), mSpinnerTimeframe, new OnFilterListener() {
//            @Override
//            public void update(int pos) {
//                onTimeframeItemSelected(pos);
//            }
//        }, mTimeEntries);
//        UiUtils.setupFilterPopupMenu(getActivity(), mSpinnerSort, new OnFilterListener() {
//            @Override
//            public void update(int pos) {
//                onSortItemSelected(pos);
//            }
//        }, mSortEntries);
//        UiUtils.setupFilterPopupMenu(getActivity(), mSpinnerList, new OnFilterListener() {
//            @Override
//            public void update(int pos) {
//                onListItemSelected(pos);
//            }
//        }, mListEntries);
//        mSpinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                onSortItemSelected(position);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        mSpinnerList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                onListItemSelected(position);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        return view;
    }

    private int getSelection(String[] values, String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout.setEnabled(mCurrentListPosition == 0);
        //        mRecyclerView.setReturningView(mFilterLayout);
        mRefreshLayout.setOnRefreshListener(this);
        //@f:off
        mRefreshLayout.setColorSchemeResources(R.color.red_light,
                R.color.green_light,
                R.color.orange_light,
                R.color.purple_light,
                R.color.blue_light);
        //@f:on
//        if (mFab != null) {
//            mFab.attachToRecyclerView(mRecyclerView);
//        }
    }

    @Override
    public void loadData() {
        getShots();
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    //    @OnItemSelected(R.id.shots_sort)
    public void onSortItemSelected(String position) {
        String pre = mSort;
        mSort = position;
        if (TextUtils.isEmpty(mSort)) {
            mSort = null;
        }
        if (isEquals(pre, mSort)) {
            return;
        }
        reloadData();
    }

    private boolean isEquals(String a, String b) {
        return ((a == null && b == null) || (a != null && a.equals(b)));
    }

    private void reloadData() {
        mCurrentPage = 1;
        //        mRecyclerView.setLoading(true);
        mRefreshLayout.setRefreshing(true);
        getShots();
    }

    //    @OnItemSelected(R.id.shots_list)
    public void onListItemSelected(String position) {
        String pre = mList;
        mList = position;
        if (TextUtils.isEmpty(mList)) {
            mList = null;
        }
        if (isEquals(pre, mList)) {
            return;
        }
        reloadData();
    }

    //    @OnItemSelected(R.id.shots_timeframe)
    public void onTimeframeItemSelected(String value) {
        String pre = mTimeframe;
        mTimeframe = value;
        if (TextUtils.isEmpty(mTimeframe)) {
            mTimeframe = null;
        }
        if (isEquals(pre, mTimeframe)) {
            return;
        }
        reloadData();
    }

    public void getShots() {
        if (!OAuthUtils.haveToken(getActivity())) {
            return;
        }
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<List<Shot>> shotsObservable = ApiFactory.getService(getActivity()).getShots(mList, mSort, mTimeframe, mCurrentPage);
        shotsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((shots) -> {
            updateData(shots);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public void isFirstItemFullVisible(boolean firstVisible) {
        mRefreshLayout.setEnabled(firstVisible);
    }

    @Override
    public void onRefresh() {
        reloadData();
    }

}
