package org.goodev.droidddle.frag.shot;

import android.os.Bundle;

import org.goodev.droidddle.frag.StatFragment;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;
import org.goodev.droidddle.widget.OnLoadingMoreListener;
import org.goodev.droidddle.widget.OnOverScrollListener;
import org.goodev.droidddle.widget.OverScrollRecyclerView;

/**
 * Created by goodev on 2014/12/25.
 */
public abstract class ObservableFragment extends StatFragment implements OnLoadingMoreListener {

    protected static final String ARG_SHOT_ID = "extra_shot_id";
    protected static final String ARG_COUNT = "extra_count";

    protected long mShotId;
    protected long mCount;
    protected int mCurrentPage = 1;
    OnOverScrollListener mOverListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mShotId = getArguments().getLong(ARG_SHOT_ID);
            mCount = getArguments().getLong(ARG_COUNT);
        }
    }

    public boolean canScrollVertically(int direction) {
        OverScrollRecyclerView mRecyclerView = getRecyclerView();
        if (mRecyclerView == null) {
            return false;
        }
        return mRecyclerView.canScrollVertically(direction);
    }

    public abstract void loadData();

    public abstract BaseAdapter getAdapter();

    public abstract OverScrollRecyclerView getRecyclerView();

    protected int getColumnCount() {
        return 1;
    }

    protected DividerItemDecoration getDivider() {
        return UiUtils.getDividerItemDecoration(getActivity().getResources());
    }

    public void setOverScrollListener(OnOverScrollListener callbacks) {
        mOverListener = callbacks;
    }


}
