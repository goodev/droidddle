package org.goodev.droidddle;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.drawee.drawable.ScalingUtils;

import org.goodev.droidddle.download.DownloadService;
import org.goodev.droidddle.drawee.ZoomableDraweeView;
import org.goodev.droidddle.utils.FrescoUtils;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.UiUtils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ViewImageActivity extends UpActivity {

    @InjectView(R.id.image_view)
    ZoomableDraweeView mImageView;
    @InjectView(R.id.image_view2)
    SubsamplingScaleImageView mImageView2;
    @InjectView(R.id.progress_layout)
    View mProgressLayout;
    @InjectView(R.id.progress)
    ProgressBar mProgressBar;
    @InjectView(R.id.progress_percentage)
    TextView mProgressPercentage;
    @InjectView(R.id.progress_values)
    TextView mProgressValues;
    String mUrl;
    String mThumbUrl;
    //    MenuItem mProgressMenu;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        //        supportRequestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_view_image);
        mUrl = getIntent().getStringExtra(UiUtils.ARG_URL);
        mThumbUrl = getIntent().getStringExtra(UiUtils.ARG_THUMB_URL);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        FrescoUtils.setShotHierarchy(this, mImageView, ScalingUtils.ScaleType.CENTER_INSIDE);
//        mImageView.setBitmapWandH(800, 600);
        //        int color = getResources().getColor(R.color.primary_color);
        //        int size = (int) (getResources().getDimension(R.dimen.keyline_1) * 2);
        //        CircularProgressDrawable mDrawable = new CircularProgressDrawable(color, 10);
        //        mDrawable.setBounds(0, 0, size, size);
        if (mUrl.endsWith(".gif")) {
            //            mImageView.setZoomable(false);
        }
        if (!TextUtils.isEmpty(mThumbUrl)) {
            FrescoUtils.setShotUrl(mImageView, mThumbUrl, null);
//            mImageView.setImageURI(Uri.parse(mThumbUrl));
            //            Glide.with(this).load(mThumbUrl).placeholder(R.drawable.placeholder).into(mImageView);
        }

        //        if (TextUtils.isEmpty(mThumbUrl)) {
        //            Glide.with(this).load(mUrl).placeholder(R.drawable.placeholder).into(mImageView);
        //        } else {
        //            DrawableRequestBuilder<?> thumbnailRequest = Glide.with(this).load(mThumbUrl);
        //            Glide.with(this).load(mUrl).placeholder(R.drawable.placeholder).thumbnail(thumbnailRequest).into(mImageView);
        //        }
        //TODO set a loading progress when loading large image..
        if (!mUrl.equals(mThumbUrl)) {
            downloadImage();
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        if (Pref.isShowFullAd(this)) {
        }

        if (Pref.isShowAd(this)) {
            int type = UiUtils.getAdsBannerType(this);
        }

    }

    //    ProgressDialog mProgressDialog;
    public void downloadImage() {
        mProgressBar.setIndeterminate(true);
        //        initProgressDialog();
        //        mProgressDialog.show();
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("url", mUrl);
        intent.putExtra("receiver", new DownloadReceiver(new Handler()));
        startService(intent);
    }

    //    @Override
    //    public boolean onCreateOptionsMenu(Menu menu) {
    //        // Inflate the menu; this adds items to the action bar if it is present.
    //        getMenuInflater().inflate(R.menu.menu_view_image, menu);
    //        mProgressMenu = menu.findItem(R.id.action_progress);
    //        MenuItemCompat.expandActionView(mProgressMenu);
    //        View view = MenuItemCompat.getActionView(mProgressMenu);
    //        ((ProgressView) view.findViewById(R.id.progress)).start();
    //        return true;
    //    }
    //
    //    @Override
    //    public boolean onOptionsItemSelected(MenuItem item) {
    //        // Handle action bar item clicks here. The action bar will
    //        // automatically handle clicks on the Home/Up button, so long
    //        // as you specify a parent activity in AndroidManifest.xml.
    //        int id = item.getItemId();
    //
    //        //noinspection SimplifiableIfStatement
    //        if (id == R.id.action_settings) {
    //            return true;
    //        }
    //
    //        return super.onOptionsItemSelected(item);
    //    }

    private String getProgressValues(long current, long total) {
        String c = UiUtils.getFileSizeString1(current);
        String t = UiUtils.getFileSizeString1(total);
        return c + "/" + t;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadService.sStop = true;
    }

    private boolean isSupportDestroyed() {
        if (Build.VERSION.SDK_INT >= 17) {
            return isDestroyed();
        }
        return false;
    }

    //    public void initProgressDialog() {
    //        // instantiate it within the onCreate method
    //        mProgressDialog = new ProgressDialog(this);
    //        mProgressDialog.setMessage("A message");
    ////        mProgressDialog.setIndeterminate(true);
    //        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    //        mProgressDialog.setCancelable(true);
    //        mProgressDialog.setIndeterminate(false);
    //        mProgressDialog.setMax(100);
    //        mProgressDialog.setProgress(0);
    //    }
    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (isFinishing() || isSupportDestroyed()) {
                return;
            }
            if (resultCode == DownloadService.UPDATE_PROGRESS) {
                mProgressBar.setIndeterminate(false);
                mProgressBar.setMax(100);
                int progress = resultData.getInt("progress");
                long total = resultData.getLong("total");
                long current = resultData.getLong("current");
                mProgressPercentage.setText(progress + "%");
                mProgressValues.setText(getProgressValues(current, total));
                mProgressBar.setProgress(progress);
                //                mProgressDialog.setProgress(progress);
                if (progress == 100) {
                    //                    mProgressDialog.dismiss();
                    mProgressLayout.setVisibility(View.GONE);
                    //                    mProgressMenu.setVisible(false);
                    String path = resultData.getString("file");
                    if (path.endsWith("gif")) {
                        FrescoUtils.setShotImage(mImageView, Uri.fromFile(new File(path)));
                    } else {
                        mImageView2.setVisibility(View.VISIBLE);
                        mImageView2.setImage(ImageSource.uri(path));
                        mImageView.setVisibility(View.GONE);
                    }
//                    final BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inJustDecodeBounds = true;
//                    BitmapFactory.decodeFile(path, options);
//                    final int height = options.outHeight;
//                    final int width = options.outWidth;
//                    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//                    Display display = wm.getDefaultDisplay();
//                    Point size = new Point();
//                    display.getSize(size);
//                    int max = Math.max(size.x, size.y);
//
//                    if (height >= max || width >= max) {
//                        mImageView2.setImageBitmap(
//                                ImageUtil.decodeSampledBitmapFromFile(path, max, max));
//                    } else {
//                        mImageView2.setImageURI(Uri.fromFile(new File(path)));
//                    }
//                    mImageView.setVisibility(View.GONE);
//                    if (height >= 2048 || width >= 2048) {
//                        mImageView.setImageBitmap(ImageUtil.decodeSampledBitmapFromFile(path, 1000, 1000));
//                    }else{
//                        FrescoUtils.setShotImage(mImageView, Uri.fromFile(new File(path)));
//                    }
//                    mImageView.setImageURI(Uri.fromFile(new File(path)));
                    //                    Glide.with(ViewImageActivity.this).load(path).into(mImageView);
                }
            }
        }
    }
}
