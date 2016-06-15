package org.goodev.droidddle;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;

import org.goodev.droidddle.frag.create.CreateBucketFragment;
import org.goodev.droidddle.frag.create.CreateShotFragment;
import org.goodev.droidddle.frag.create.SaveCheckFragment;
import org.goodev.droidddle.pojo.Bucket;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.UiUtils;

import butterknife.ButterKnife;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;


public class CreateActivity extends UpActivity {

    int mType;
    SaveCheckFragment mFragment;
    Bucket mBucket;
    Shot mShot;
    final PermissionCallback permissionContactsCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
        }

        @Override
        public void permissionRefused() {
        }
    };

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_create);
        ButterKnife.inject(this);
        Nammu.init(getApplicationContext());
        mType = getIntent().getIntExtra(UiUtils.ARG_TYPE, UiUtils.TYPE_SHOT);
        if (mType == UiUtils.TYPE_BUCKET) {
            mBucket = getIntent().getParcelableExtra(UiUtils.ARG_BUCKET);
        } else {
            mShot = getIntent().getParcelableExtra(UiUtils.ARG_SHOT);
        }
        if (savedInstanceState == null) {
            mFragment = getFrament();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, mFragment).commit();
        } else {
            mFragment = (SaveCheckFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle();
    }

    private void setTitle() {
        switch (mType) {
            case UiUtils.TYPE_BUCKET:
                if (mBucket != null) {
                    setTitle(R.string.title_activity_update_bucket);
                } else {
                    setTitle(R.string.title_activity_create_bucket);
                }
                break;
            case UiUtils.TYPE_SHOT:
                if (mShot != null) {
                    setTitle(R.string.title_activity_update_shot);
                } else {
                    setTitle(R.string.title_activity_create_shot);
                }
                break;
        }
    }

    private SaveCheckFragment getFrament() {
        SaveCheckFragment fragment = null;
        switch (mType) {
            case UiUtils.TYPE_BUCKET:
                fragment = CreateBucketFragment.newInstance(mBucket);
                break;
            case UiUtils.TYPE_SHOT:
                fragment = CreateShotFragment.newInstance(mShot);
                break;
        }
        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!checkToShowDiscardDialog()) {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkToShowDiscardDialog() {
        boolean alert = mFragment != null && mFragment.needSaveAlert();
        if (alert) {
            //@f:off
            new AlertDialog.Builder(this).setMessage(R.string.discard_tips)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
            //        new MaterialDialog.Builder(this)
            //                .content(R.string.discard_tips)
            //                .negativeText(android.R.string.cancel)
            //                .positiveText(android.R.string.ok)
            //                .callback(new MaterialDialog.Callback() {
            //                    @Override
            //                    public void onNegative(MaterialDialog materialDialog) {
            //
            //                    }@Override
            //                    public void onPositive(MaterialDialog materialDialog) {
            //                        finish();
            //                    }})
            //                .show();
            //@f:on
        }
        return alert;
    }

    @Override
    public void onBackPressed() {
        if (checkToShowDiscardDialog()) {

        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                //Now he/she can mark "never ask again" (sic!)
                Snackbar.make(findViewById(R.id.image_view_tips), "Need storage permission to read pictures.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Nammu.askForPermission(CreateActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, permissionContactsCallback);
                            }
                        }).show();
            } else {
                //First time asking for permission
                // or phone doesn't offer permission
                // or user marked "never ask again"
                Nammu.askForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, permissionContactsCallback);
            }
        }
    }


}
