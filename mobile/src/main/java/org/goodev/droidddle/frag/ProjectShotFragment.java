package org.goodev.droidddle.frag;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.api.SucessCallback;
import org.goodev.droidddle.frag.team.BaseTeamFragment;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.DividerItemDecoration;
import org.goodev.droidddle.widget.OnOperationListener;

import java.util.List;

import retrofit.client.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by goodev on 2014/12/31.
 */
public class ProjectShotFragment extends BaseTeamFragment<Shot> implements OnOperationListener<Shot> {

    private ProjectShotAdapter mAdatper;

    private long mId;
    private int mType;
    private boolean mIsSelf;

    public ProjectShotFragment() {
    }

    public static ProjectShotFragment newInstance(long id, int type) {
        return newInstance(id, type, false);
    }

    public static ProjectShotFragment newInstance(long id, int type, boolean self) {
        ProjectShotFragment fragment = new ProjectShotFragment();
        Bundle args = new Bundle();
        args.putBoolean(UiUtils.ARG_SELF, self);
        args.putLong(UiUtils.ARG_ID, id);
        args.putInt(UiUtils.ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    protected DividerItemDecoration getDivider() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getArguments().getLong(UiUtils.ARG_ID);
        mIsSelf = getArguments().getBoolean(UiUtils.ARG_SELF, false);
        mType = getArguments().getInt(UiUtils.ARG_TYPE);
        mAdatper = new ProjectShotAdapter(getActivity());
        mAdatper.setIsBucket(mType == UiUtils.TYPE_BUCKET);
        if (mIsSelf && mType == UiUtils.TYPE_BUCKET) {
            mAdatper.setIsSelf(mIsSelf);
            mAdatper.setOperationListener(this);
        }
    }


    public void loadData() {
        Observable<List<Shot>> observable = null;
        if (mType == UiUtils.TYPE_PROJECT) {
            observable = ApiFactory.getService(getActivity()).getProjectShots(mId, mCurrentPage);
        } else {
            observable = ApiFactory.getService(getActivity()).getBucketShots(mId, mCurrentPage);
        }
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    //    @Override
    //    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    //        super.onViewCreated(view, savedInstanceState);
    //
    //        if (mIsSelf && mType == UiUtils.TYPE_BUCKET) {
    //            setUpSwipeToRemove();
    //        }
    //
    //    }

    @Override
    protected int getColumnCount() {
        return getResources().getInteger(R.integer.shot_column);
    }
    //    private void setUpSwipeToRemove() {
    //        SwipeUtils.CallbackAdapter callbacks = new SwipeUtils.CallbackAdapter() {
    //            @Override
    //            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions, boolean right) {
    //                for (int position : reverseSortedPositions) {
    //                    Shot shot = mAdatper.getItem(position);
    //                    removeShotFromBucket(shot);
    //                    mAdatper.remove(position);
    //                }
    //                mAdatper.notifyDataSetChanged();
    //            }
    //        };
    //        SwipeUtils.OnItemClickListener itemClickListener = new SwipeUtils.OnItemClickListener() {
    //            @Override
    //            public void onItemClick(View view, int position) {
    //                Shot shot = mAdatper.getItem(position);
    //                UiUtils.launchShot(getActivity(), shot);
    //            }
    //        };
    //        SwipeUtils.setupSwipe(getActivity(), getRecyclerView(), callbacks, itemClickListener);
    //
    //
    //    }

    private void removeShotFromBucket(Shot shot) {
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mType == UiUtils.TYPE_BUCKET) {
            Observable<Response> observable = ApiFactory.getService(getActivity()).deleteShotFromBucket(mId, shot.id);
            observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SucessCallback<Response>(getActivity(), R.string.shot_removed_from_bucket), new ErrorCallback(getActivity()));
        }
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdatper;
    }

    @Override
    public void update(Shot data, int position) {

    }

    @Override
    public void delete(Shot data, int position) {
        removeShotFromBucket(data);
        mAdatper.remove(position);
    }


    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        GestureDetector mGestureDetector;
        private OnItemClickListener mListener;

        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            //TODO ..
        }
    }

}
