package org.goodev.droidddle.frag.user;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.goodev.droidddle.CreateActivity;
import org.goodev.droidddle.MainActivity;
import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.api.SucessCallback;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.OAuthUtils;
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
 * Created by ADMIN on 2014/12/29.
 */
public class UserShotFragment extends SwipeUserFragment<Shot, Shot> implements OnOperationListener<Shot> {

    private static final int CODE = 1;
    Dialog mDialog;
    private UserShotAdapter mAdatper;
    private boolean mIsSelf;

    public UserShotFragment() {
    }

    public static UserShotFragment newInstance(User user) {
        return BaseUserFragment.newInstance(UserShotFragment.class, user);
    }

    protected DividerItemDecoration getDivider() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsSelf = OAuthUtils.isSelf(mUser.id);
        mAdatper = new UserShotAdapter(getActivity(), mUser, mIsSelf);
        if (mIsSelf) {
            mAdatper.setOperationListener(this);
        }
        setHasOptionsMenu(false);
    }

    public void loadData() {
        Observable<List<Shot>> observable = ApiFactory.getService(getActivity()).getUserShots(mUser.id, mCurrentPage);
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
        item.setTitle(R.string.add_action_shot);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                createShot();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createShot() {
        //        UiUtils.showNeedInvitationDialog(getActivity());
        //        if (true) return;
        Intent intent = new Intent(getActivity(), CreateActivity.class);
        intent.putExtra(UiUtils.ARG_TYPE, UiUtils.TYPE_SHOT);
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

    //    @Override
    //    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    //        super.onViewCreated(view, savedInstanceState);
    //        if (mIsSelf) {
    //            setUpSwipeToRemove();
    //        }
    //    }
    //    private void setUpSwipeToRemove() {
    //        SwipeUtils.CallbackAdapter callbacks = new SwipeUtils.CallbackAdapter() {
    //            @Override
    //            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions, boolean right) {
    //                for (int position : reverseSortedPositions) {
    //                    //                                    mLayoutManager.removeView(mLayoutManager.getChildAt(position));
    //                    Shot data = mAdatper.getItem(position);
    //                    if (right) {
    //                        updateShot(data);
    //                        return;
    //                    } else {
    //                        removeItemDelayed(data);
    //                        mAdatper.remove(position);
    //                        //mAdatper.notifyItemRemoved(position);
    //                    }
    //                }
    //                mAdatper.notifyDataSetChanged();
    //            }
    //        };
    //
    //        SwipeUtils.OnItemClickListener itemClickListener = new SwipeUtils.OnItemClickListener() {
    //            @Override
    //            public void onItemClick(View view, int position) {
    //                Shot data = mAdatper.getItem(position);
    //                if (data.user == null) {
    //                    data.user = mUser;
    //                }
    //                UiUtils.launchShot(getActivity(), data, view.findViewById(R.id.shot_image));
    //            }
    //        };
    //
    //        SwipeUtils.setupSwipe(getActivity(), getRecyclerView(), callbacks, itemClickListener);
    //
    //    }

    @Override
    protected int getColumnCount() {
        if (getActivity() instanceof MainActivity) {
            return getResources().getInteger(R.integer.shot_column);
        }
        return getResources().getInteger(R.integer.user_shot_column);
    }

    private void updateShot(Shot data) {
        Intent intent = new Intent(getActivity(), CreateActivity.class);
        intent.putExtra(UiUtils.ARG_TYPE, UiUtils.TYPE_SHOT);
        intent.putExtra(UiUtils.ARG_SHOT, data);
        startActivityForResult(intent, CODE);
    }

    @Override
    protected void removeItem(Shot data) {
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<Response> observable = ApiFactory.getService(getActivity()).deleteShot(data.id);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SucessCallback<Response>(getActivity(), R.string.shot_removed, mDialog), new ErrorCallback(getActivity(), mDialog));

    }

    @Override
    protected int getUndoMessage() {
        return R.string.shot_removed_tips;
    }

    @Override
    public void update(Shot data, int position) {
        updateShot(data);
    }

    @Override
    public void delete(Shot data, int position) {
        UiUtils.showConfirmDialog(getActivity(), R.string.dialog_delete_shot_msg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteShot(data, position);
                    }
                });

    }

    private void deleteShot(Shot data, int position) {
        mDialog = UiUtils.showProgressDialog(getActivity(), R.string.deleting);
        if (position >= 0) {
            mAdatper.remove(position);
        }
        removeItem(data);
    }

}
