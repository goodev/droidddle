package org.goodev.droidddle.frag.user;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.goodev.droidddle.CreateActivity;
import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.api.SucessCallback;
import org.goodev.droidddle.holder.OnClickListener;
import org.goodev.droidddle.pojo.Bucket;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.BaseAdapter;
import org.goodev.droidddle.widget.OnOperationListener;

import java.util.List;

import retrofit.client.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ADMIN on 2014/12/29.
 */
public class UserBucketFragment extends SwipeUserFragment<Bucket, Bucket> implements OnOperationListener<Bucket> {

    private static final int CODE = 1;
    Dialog mDialog;
    private UserBucketAdapter mAdatper;
    private OnClickListener<Bucket> mBucketListener;
    private boolean mIsSelf;

    public UserBucketFragment() {
    }

    public static UserBucketFragment newInstance(User user) {
        return BaseUserFragment.newInstance(UserBucketFragment.class, user);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsSelf = OAuthUtils.isSelf(mUser.id);
        mAdatper = new UserBucketAdapter(getActivity(), mBucketListener, mIsSelf);
        if (mIsSelf) {
            //mBucketListener != null is means pick a bucket. so do not enable swipe to remove
            if (mBucketListener == null) {
                mAdatper.setOperationListener(this);
            }
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnClickListener) {
            mBucketListener = (OnClickListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mBucketListener = null;
    }

    public void loadData() {
        Observable<List<Bucket>> observable = ApiFactory.getService(getActivity()).getUserBuckets(mUser.id, mCurrentPage);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((data) -> {
            updateData(data);
        }, new ErrorCallback(getActivity()));
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdatper;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_ment, menu);
        MenuItem item = menu.findItem(R.id.action_add);
        item.setTitle(R.string.add_action_bucket);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                createBucket();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createBucket() {
        Intent intent = new Intent(getActivity(), CreateActivity.class);
        intent.putExtra(UiUtils.ARG_TYPE, UiUtils.TYPE_BUCKET);
        startActivityForResult(intent, CODE);
    }

    private void updateBucket(Bucket bucket) {
        Intent intent = new Intent(getActivity(), CreateActivity.class);
        intent.putExtra(UiUtils.ARG_TYPE, UiUtils.TYPE_BUCKET);
        intent.putExtra(UiUtils.ARG_BUCKET, bucket);
        startActivityForResult(intent, CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && CODE == requestCode) {
            //Reload data
            loadFirstPageData();
        }
    }

    //    private void setUpSwipeToRemove() {
    //        SwipeUtils.CallbackAdapter callbacks = new SwipeUtils.CallbackAdapter() {
    //            @Override
    //            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions, boolean right) {
    //                L.d("right " + right);
    //                for (int position : reverseSortedPositions) {
    //                    // mLayoutManager.removeView(mLayoutManager.getChildAt(position));
    //                    Bucket bucket = mAdatper.getItem(position);
    //                    if (right) {
    //                        updateBucket(bucket);
    //                        return;
    //                    } else {
    //                        removeItemDelayed(bucket);
    //                        mAdatper.remove(position);
    //                        //mAdatper.notifyItemRemoved(position);
    //                    }
    //                }
    //                mAdatper.notifyDataSetChanged();
    //            }
    //        };
    //        SwipeUtils.OnItemClickListener itemClickListener = new SwipeUtils.OnItemClickListener() {
    //            @Override
    //            public void onItemClick(View view, int position) {
    //                Bucket bucket = mAdatper.getItem(position);
    //                UiUtils.launchBucketShots(getActivity(), bucket, mIsSelf);
    //            }
    //        };
    //        SwipeUtils.setupSwipe(getActivity(), getRecyclerView(), callbacks, itemClickListener);
    //
    //
    //    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //mBucketListener != null is means pick a bucket. so do not enable swipe to remove
        //        if (mIsSelf && mBucketListener == null) {
        //            setUpSwipeToRemove();
        //        }

    }

    protected void removeItem(Bucket data) {
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<Response> observable = ApiFactory.getService(getActivity()).deleteBucket(data.id);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SucessCallback<Response>(getActivity(), R.string.bucket_removed, mDialog), new ErrorCallback(getActivity(), mDialog));
    }

    @Override
    protected int getUndoMessage() {
        return R.string.bucket_removed_tips;
    }

    @Override
    public void update(Bucket data, int position) {
        updateBucket(data);
    }

    @Override
    public void delete(Bucket data, int position) {
        UiUtils.showConfirmDialog(getActivity(), R.string.dialog_delete_bucket_msg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBucket(data, position);
                    }
                });

    }

    private void deleteBucket(Bucket data, int position) {
        mDialog = UiUtils.showProgressDialog(getActivity(), R.string.deleting);
        if (position >= 0) {
            mAdatper.remove(position);
        }
        removeItem(data);
    }
}
