package org.goodev.droidddle.receiver;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.download.DownloadService;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.Utils;
import org.goodev.droidddle.widget.GoURLSpan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by goodev on 2015/1/22.
 */
public class ShotWidgetService extends RemoteViewsService {
    public static Bitmap downloadImage(Context context, String fileUrl) {
        String file = DownloadService.getShotImageFile(context, fileUrl);
        int fileLength = 0;
        try {
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            fileLength = connection.getContentLength();
            File imageFile = new File(file);
            if (imageFile.exists() && imageFile.length() == fileLength) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(file);
                    return bitmap;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());

            OutputStream output = new FileOutputStream(file);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this, intent);
    }

    static class StackRemoteViewsFactory implements RemoteViewsFactory {
        private static final int mCount = 12;
        private List<Shot> mWidgetItems = new ArrayList<Shot>();
        private Context mContext;
        private int mAppWidgetId;

        public StackRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        public void onCreate() {
            // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
            // for example downloading or creating content etc, should be deferred to onDataSetChanged()
            // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
            //            List<Shot> shots = ApiFactory.getService(mContext).getShotsSync(null, "recent", null, 1);
            //            updateShots(shots);
            //            Observable<List<Shot>> shots = ApiFactory.getService(mContext).getShots(null, "recent", null, 1);
            //            shots.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread()).subscribe((list) -> {
            //                updateShots(list);
            //            }, new ErrorCallback(mContext));
        }

        @Override
        public void onDataSetChanged() {
            if (!Utils.hasInternet(mContext)) {
                Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
                return;
            }
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            try {
                ShotAppWidgetProvider.setupWidget(mContext, appWidgetManager, mAppWidgetId, true);
                List<Shot> shots = ApiFactory.getService(mContext).getShotsSync(null, "recent", null, 1);
                updateShots(shots);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ShotAppWidgetProvider.setupWidget(mContext, appWidgetManager, mAppWidgetId, false);
        }

        private void updateShots(List<Shot> list) {
            mWidgetItems.clear();
            mWidgetItems.addAll(list);
        }

        @Override
        public void onDestroy() {
            mWidgetItems.clear();
        }

        @Override
        public int getCount() {
            return mWidgetItems.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.shot_widget_item);
            if (position >= mWidgetItems.size()) {
                return rv;
            }
            Shot shot = mWidgetItems.get(position);
            // Construct a remote views item based on the app widget item XML file,
            // and set the text based on the position.

            // Set the click intent so that we can handle it and show a toast message
            final Intent fillInIntent = new Intent();
            final Bundle extras = new Bundle();
            extras.putLong(ShotAppWidgetProvider.EXTRA_SHOT_ID, shot.id);
            fillInIntent.putExtras(extras);
            rv.setOnClickFillInIntent(R.id.shot_item_layout, fillInIntent);

            rv.setTextViewText(R.id.shot_title, shot.title);
            //           rv.setTextViewText(R.id.shot_views, UiUtils.getCountValue(shot.viewsCount)+"");

            if (shot.createdAt != null) {
                CharSequence date = DateUtils.getRelativeTimeSpanString(shot.createdAt.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE | DateUtils.FORMAT_NUMERIC_DATE);
                rv.setTextViewText(R.id.shot_date, date);
            } else {
                rv.setTextViewText(R.id.shot_date, null);
            }
            if (TextUtils.isEmpty(shot.description)) {
                rv.setTextViewText(R.id.shot_description, null);
                rv.setViewVisibility(R.id.shot_description, View.INVISIBLE);
            } else {
                rv.setViewVisibility(R.id.shot_description, View.VISIBLE);
                Spanned spannable = GoURLSpan.hackURLSpan(Html.fromHtml(shot.description));
                rv.setTextViewText(R.id.shot_description, spannable);
            }
            //            AppWidgetTarget target = new AppWidgetTarget(mContext, rv, R.id.shot_image, mAppWidgetId);
            Bitmap bitmap = downloadImage(mContext, shot.images.normal);
            rv.setImageViewBitmap(R.id.shot_image, bitmap);
            //           Glide.with(mContext).load(shot.images.normal).asBitmap().into(target);
            // Return the remote views object.

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_loading_view);
            return rv;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
