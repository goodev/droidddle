package org.goodev.droidddle.frag.create;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.FileUtils;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ADMIN on 2015/1/2.
 */
public class CreateShotFragment extends SaveCheckFragment {

    private static final int REQ_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CROP_IMAGE = 2;
    @InjectView(R.id.edit_text_name)
    EditText mNameView;
    @InjectView(R.id.edit_text_description)
    EditText mDescriptionView;
    @InjectView(R.id.edit_text_tags)
    EditText mTagsView;
    @InjectView(R.id.image_view)
    ImageView mImageView;
    @InjectView(R.id.image_view_label)
    View mImageViewLabel;
    @InjectView(R.id.image_view_tips)
    View mImageViewTips;

    MenuItem mSendMenu;
    File mImageFile;
    String mMimeType;
    private Dialog mDialog;
    private Shot mShot;

    public CreateShotFragment() {
        // Required empty public constructor
    }

    public static CreateShotFragment newInstance(Shot shot) {
        CreateShotFragment fragment = new CreateShotFragment();
        if (shot != null) {
            Bundle args = new Bundle();
            args.putParcelable(UiUtils.ARG_SHOT, shot);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mShot = getArguments().getParcelable(UiUtils.ARG_SHOT);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_shot, container, false);
        ButterKnife.inject(this, view);

        mDescriptionView
                .setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_CLASS_TEXT);
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
        if (mShot != null) {
            enterEditModel();
        }
        return view;
    }

    private void enterEditModel() {
        mNameView.setText(UiUtils.removePTag(mShot.title));
        mDescriptionView.setText(UiUtils.removePTag(mShot.description));
        mTagsView.setText(UiUtils.getTagsString(mShot.tags));
        mImageView.setVisibility(View.GONE);
        mImageViewLabel.setVisibility(View.GONE);
        mImageViewTips.setVisibility(View.GONE);
    }

    private void updateMenuStatus(String text) {
        if (mSendMenu != null) {
            boolean hasTitle = !TextUtils.isEmpty(text);
            boolean enabled = hasTitle && (mShot != null || mImageFile != null);
            mSendMenu.setEnabled(enabled);
        }
    }

    final PermissionCallback permissionContactsCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
        }

        @Override
        public void permissionRefused() {
        }
    };

    @OnClick(R.id.image_view)
    void pickImage(View view) {
        if (!Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                //Now he/she can mark "never ask again" (sic!)
                Snackbar.make(mImageViewTips, "Need storage permission to read pictures.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Nammu.askForPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, permissionContactsCallback);
                            }
                        }).show();
            } else {
                //First time asking for permission
                // or phone doesn't offer permission
                // or user marked "never ask again"
                Nammu.askForPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, permissionContactsCallback);
            }
            return;
        }
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        //        photoPickerIntent.putExtra("crop", "true");
        //        photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
        //        photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQ_CODE_PICK_IMAGE) {
            Uri uri = data.getData();
            String filePath = FileUtils.getPath(getActivity(), uri);
            if (TextUtils.isEmpty(filePath)) {
                UiUtils.showToast(getActivity(), R.string.pick_image_error);
                return;
            }
            mImageFile = new File(filePath);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts);
            if (TextUtils.isEmpty(opts.outMimeType)) {
                mMimeType = FileUtils.getMimeType(filePath);
            } else {
                mMimeType = opts.outMimeType;
            }

            if (needCrop(opts.outWidth, opts.outHeight)) {
                //                UiUtils.grantPermission(getActivity(), uri, data.getFlags());
                mImageFile = UiUtils.getTempFile(getActivity());
                startCropImage(uri, Uri.fromFile(mImageFile));
            } else {
                setupImage();
                updateMenuStatus(mNameView.getText().toString().trim());
            }
        } else if (requestCode == REQUEST_CROP_IMAGE) {
            mMimeType = "image/png";
            setupImage();
            updateMenuStatus(mNameView.getText().toString().trim());
        }
    }

    private void setupImage() {
        Bitmap bitmap = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
        mImageView.setImageBitmap(bitmap);
    }

    //@f:off
    private void startCropImage(Uri uri, Uri saveUri) {
        Intent intent = new CropImageIntentBuilder(UiUtils.SMALL_WIDTH, UiUtils.SMALL_HEIGHT, UiUtils.SMALL_WIDTH, UiUtils.SMALL_HEIGHT, saveUri)
                .setSourceImage(uri)
                .setDoFaceDetection(false)
                .setScaleUpIfNeeded(false)
                .getIntent(getActivity());
        startActivityForResult(intent, REQUEST_CROP_IMAGE);
    }

    //@f:on
    private void startCropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //        intent.setClassName("com.android.camera", "com.android.camera.CropImage");
        intent.setData(uri);
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 400);
        intent.putExtra("outputY", 300);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        PackageManager pm = getActivity().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null) {
            //can crop image by system
            startActivityForResult(intent, REQUEST_CROP_IMAGE);
        } else {
        }

    }

    private boolean needCrop(int width, int height) {
        boolean small = width == UiUtils.SMALL_WIDTH && height == UiUtils.SMALL_HEIGHT;
        boolean big = width == UiUtils.BIG_WIDTH && height == UiUtils.BIG_HEIGHT;
        return !small && !big;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_comment, menu);
        mSendMenu = menu.findItem(R.id.action_send);
        mSendMenu.setEnabled(mShot != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            if (mShot == null) {
                createShot();
            } else {
                updateShot();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createShot() {
        if (mImageFile == null || !mImageFile.exists()) {
            UiUtils.showToast(getActivity(), R.string.shot_image_file_missing);
            return;
        }
        if (mMimeType == null) {
            mMimeType = FileUtils.getMimeType(mImageFile);
        }
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        mDialog = UiUtils.showProgressDialog(getActivity(), getString(R.string.creating));
        String text = mNameView.getText().toString();
        String des = mDescriptionView.getText().toString();
        String tag = mTagsView.getText().toString().trim();
        //TODO set image/png GIF, JPG by file ext name
        TypedFile image = new TypedFile(mMimeType, mImageFile);
        TypedString title = new TypedString(text);
        TypedString description = new TypedString(des);
        TypedString tags = new TypedString(tag);
        Observable<Response> observable = ApiFactory.getService(getActivity()).createShot(image, title, description, tags);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((response) -> {
            bucketCreated(response);
        }, new ErrorCallback(getActivity()));

    }

    private void updateShot() {
        String title = mNameView.getText().toString();
        String des = mDescriptionView.getText().toString();
        //TODO set image/png GIF, JPG by file ext name
        String tag = mTagsView.getText().toString().trim();
        if (!Utils.hasInternet(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        mDialog = UiUtils.showProgressDialog(getActivity(), getString(R.string.creating));
        //        String tags = TextUtils.isEmpty(tag) ? null : tag ;
        if (Pref.getSendByShot(getActivity())) {
            des += getString(R.string.send_by_text);
        }
        Observable<Shot> observable = ApiFactory.getService(getActivity()).updateShot(mShot.id, title, des, tag);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((response) -> {
            shotUpdated(response);
        }, new ErrorCallback(getActivity(), mDialog));

    }

    private void shotUpdated(Shot shot) {

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        UiUtils.dismissDialog(mDialog);
        UiUtils.showToast(activity, R.string.shot_updated);
        activity.setResult(Activity.RESULT_OK);
        activity.finish();
    }

    private void bucketCreated(Response data) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        UiUtils.dismissDialog(mDialog);
        if (data.getStatus() == 202) {
            UiUtils.showToast(activity, R.string.shot_created);
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
        }
    }

    @Override
    public boolean needSaveAlert() {
        String title = mNameView.getText().toString();
        String des = mDescriptionView.getText().toString();
        //TODO set image/png GIF, JPG by file ext name
        String tag = mTagsView.getText().toString().trim();
        return mImageFile != null || !TextUtils.isEmpty(title) || !TextUtils.isEmpty(des) || !TextUtils.isEmpty(tag);
    }
}
