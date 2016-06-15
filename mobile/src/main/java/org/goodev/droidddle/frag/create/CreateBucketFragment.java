package org.goodev.droidddle.frag.create;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Bucket;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateBucketFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateBucketFragment extends SaveCheckFragment {
    @InjectView(R.id.edit_text_name)
    EditText mNameView;
    @InjectView(R.id.edit_text_description)
    EditText mDescriptionView;
    MenuItem mSendMenu;
    Bucket mBucket;
    Dialog mDialog;

    public CreateBucketFragment() {
        // Required empty public constructor
    }

    public static CreateBucketFragment newInstance(Bucket bucket) {
        CreateBucketFragment fragment = new CreateBucketFragment();
        if (bucket != null) {
            Bundle args = new Bundle();
            args.putParcelable(UiUtils.ARG_BUCKET, bucket);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBucket = getArguments().getParcelable(UiUtils.ARG_BUCKET);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_bucket, container, false);
        ButterKnife.inject(this, view);

        mNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateMenuStatus(s.toString().trim());
            }
        });

        if (mBucket != null) {
            mDescriptionView.setText(UiUtils.removePTag(mBucket.description));
            mNameView.setText(UiUtils.removePTag(mBucket.name));
        }
        return view;
    }

    private void updateMenuStatus(String text) {
        if (mSendMenu != null)
            mSendMenu.setEnabled(!TextUtils.isEmpty(text));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_comment, menu);
        mSendMenu = menu.findItem(R.id.action_send);
        mSendMenu.setEnabled(mBucket != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            if (mBucket == null) {
                addBucket();
            } else {
                updateBucket();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateBucket() {
        String text = mNameView.getText().toString();
        String des = mDescriptionView.getText().toString();
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        mDialog = UiUtils.showProgressDialog(getActivity(), getString(R.string.creating));
        Observable<Bucket> observable = ApiFactory.getService(getActivity()).editBucket(mBucket.id, text, des);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((bucket) -> {
            bucketCreated(bucket);
        }, new ErrorCallback(getActivity()));
    }

    private void addBucket() {
        String text = mNameView.getText().toString();
        String des = mDescriptionView.getText().toString();
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        mDialog = UiUtils.showProgressDialog(getActivity(), getString(R.string.creating));
        Observable<Bucket> observable = ApiFactory.getService(getActivity()).postBucket(text, des);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((bucket) -> {
                    bucketCreated(bucket);
                }, new ErrorCallback(getActivity()));

    }

    private void bucketCreated(Bucket bucket) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        UiUtils.dismissDialog(mDialog);
        UiUtils.showToast(activity, mBucket == null ? R.string.bucket_created : R.string.bucket_updated);
        activity.setResult(Activity.RESULT_OK);
        activity.finish();
    }

    @Override
    public boolean needSaveAlert() {
        String title = mNameView.getText().toString();
        String des = mDescriptionView.getText().toString();
        return !TextUtils.isEmpty(title) || !TextUtils.isEmpty(des);
    }
}
