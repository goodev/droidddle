package org.goodev.droidddle.holder;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.api.SucessCallback;
import org.goodev.droidddle.pojo.Attachment;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.client.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ADMIN on 2015/1/2.
 */
public class AttachmentViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.image_view)
    SimpleDraweeView mImageView;
    @InjectView(R.id.file_size)
    TextView mFileSizeView;
    @InjectView(R.id.file_name)
    TextView mFileNameView;
    @InjectView(R.id.counts_view)
    TextView mViewsCountView;
    @InjectView(R.id.download_view)
    View mDownloadView;
    @InjectView(R.id.delete_view)
    View mDeleteView;


    Context mContext;
    Attachment data;
    long mShotId;
    Dialog mDialog;

    public AttachmentViewHolder(View view, Context context) {
        super(view);
        mContext = context;
        ButterKnife.inject(this, view);
    }

    @OnClick({R.id.image_view, R.id.download_view, R.id.delete_view})
    void clickImage(View view) {
        switch (view.getId()) {
            case R.id.image_view:
                UiUtils.launchImage(mContext, data.url, data.thumbnailUrl);
                break;
            case R.id.download_view:
                if (UiUtils.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    downloadFile();
                } else {
                    Toast.makeText(mContext, R.string.sdcard_permission_tips, Toast.LENGTH_LONG).show();
                    UiUtils.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                }
                break;
            case R.id.delete_view:
                deleteFile();
                break;
        }
    }

    private void deleteFile() {
        //@formatter:off
        new AlertDialog.Builder(mContext).setTitle(R.string.delete_attach_dialog_title)
                .setMessage(R.string.delete_attach_dialog_message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAttachment();
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();

//        new MaterialDialog.Builder(mContext).title(R.string.delete_attach_dialog_title).content(R.string.delete_attach_dialog_message).positiveText(R.string.delete).negativeText(android.R.string.cancel).callback(new MaterialDialog.SimpleCallback() {
//            @Override
//            public void onPositive(MaterialDialog materialDialog) {
//                deleteAttachment();
//            }
//        }).show();
        //@formatter:on
    }

    //@f:off
    private void deleteAttachment() {
        if (!Utils.hasInternet(mContext)) {
            Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        mDialog = UiUtils.showProgressDialog(mContext, mContext.getString(R.string.deleting));
        Observable<Response> observable = ApiFactory.getService(mContext).deleteAttachments(mShotId, data.id);
        observable.subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(new SucessCallback<Response>(mContext, R.string.attach_file_deleted, mDialog), new ErrorCallback(mContext, mDialog));
    }
    //@f:on

    private void downloadFile() {
        UiUtils.downloadFile(mContext, data.url, mShotId);
    }

    public void setData(Attachment data) {
        setData(data, false, UiUtils.NO_ID);
    }

    public void setData(Attachment data, boolean isSelf, long shotId) {
        mDeleteView.setVisibility(isSelf ? View.VISIBLE : View.GONE);
        this.data = data;
        this.mShotId = shotId;
        boolean isImage = UiUtils.isImage(data.contentType);
        if (isImage) {
            mImageView.setVisibility(View.VISIBLE);
            String url = TextUtils.isEmpty(data.thumbnailUrl) ? data.url : data.thumbnailUrl;
            mImageView.setImageURI(Uri.parse(url));
            //            Glide.with(mContext).load(url).placeholder(R.drawable.placeholder).into(mImageView);
            mFileNameView.setText(null);
            mFileNameView.setVisibility(View.GONE);
        } else {
            mFileNameView.setVisibility(View.VISIBLE);
            mFileNameView.setText(UiUtils.getFileName(data.url));
            mImageView.setImageDrawable(null);
            mImageView.setVisibility(View.GONE);
        }

        mFileSizeView.setText(UiUtils.getFileSizeString(data.size));
        mViewsCountView.setText(UiUtils.intToString(data.viewsCount));
    }
}
