package org.goodev.droidddle.receiver;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import org.goodev.droidddle.R;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.UiUtils;

/**
 * Created by goodev on 2015/1/22.
 */
public class ShotAppWidgetProvider extends AppWidgetProvider {
    public static final String EXTRA_SHOT_ID = UiUtils.ARG_SHOT_ID;
    public static final String REFRESH_ACTION = "org.goodev.droidddle.REFRESH";
    public static final String CLICK_ACTION = "org.goodev.droidddle.CLICK";

    public static void setupWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        setupWidget(context, appWidgetManager, appWidgetId, false);
    }

    public static void setupWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, boolean refreshing) {
        boolean oauthed = OAuthUtils.haveToken(context);
        // Set up the intent that starts the StackViewService, which will
        // provide the views for this collection.
        Resources res = context.getResources();
        Intent intent = new Intent(context, ShotWidgetService.class);
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        // Instantiate the RemoteViews object for the app widget layout.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.shot_appwidget);
        // Set up the RemoteViews object to use a RemoteViews adapter.
        // This adapter connects
        // to a RemoteViewsService  through the specified intent.
        // This is how you populate the data.
        rv.setImageViewResource(R.id.shot_widget_update, R.drawable.ic_refresh_grey);
        //            rv.setImageViewResource(R.id.shot_widget_icon, R.drawable.ic_launcher);
        rv.setTextViewText(R.id.shot_widget_title, res.getString(R.string.shot_widget_title));
        rv.setViewVisibility(R.id.shot_widget_progress, refreshing ? View.VISIBLE : View.INVISIBLE);
        rv.setViewVisibility(R.id.shot_widget_update, refreshing ? View.INVISIBLE : View.VISIBLE);

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        rv.setEmptyView(R.id.shot_stack_view, R.id.shot_widget_empty_view);
        rv.setTextViewText(R.id.shot_widget_empty_view, res.getString(R.string.shot_widget_empty));
        if (oauthed) {
            rv.setRemoteAdapter(R.id.shot_stack_view, intent);
            rv.setViewVisibility(R.id.shot_widget_tips_view, View.GONE);
        } else {
            rv.setTextViewText(R.id.shot_widget_tips_view, res.getString(R.string.not_login));
            rv.setViewVisibility(R.id.shot_widget_tips_view, View.VISIBLE);
        }

        final Intent onClickIntent = new Intent(context, ShotAppWidgetProvider.class);
        onClickIntent.setAction(ShotAppWidgetProvider.CLICK_ACTION);
        onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.shot_stack_view, onClickPendingIntent);

        final Intent refreshIntent = new Intent(context, ShotAppWidgetProvider.class);
        refreshIntent.setAction(ShotAppWidgetProvider.REFRESH_ACTION);
        final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.shot_widget_update, refreshPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, rv);
        final ComponentName cn = new ComponentName(context, ShotAppWidgetProvider.class);
        //            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(cn), R.id.shot_stack_view);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (action.equals(REFRESH_ACTION)) {
            // BroadcastReceivers have a limited amount of time to do work, so for this sample, we
            // are triggering an update of the data on another thread.  In practice, this update
            // can be triggered from a background service, or perhaps as a result of user actions
            // inside the main application.
            final Context context = ctx;
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.shot_stack_view);
            } else {
                final ComponentName cn = new ComponentName(context, ShotAppWidgetProvider.class);
                mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.shot_stack_view);
            }

        } else if (action.equals(CLICK_ACTION)) {
            final long id = intent.getLongExtra(EXTRA_SHOT_ID, -1);
            if (id != -1) {
                UiUtils.launchShot(ctx, id);
            }
        }

        super.onReceive(ctx, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int N = appWidgetIds.length;
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            //            setupNextRefresh(context, appWidgetIds[i]);
            setupWidget(context, appWidgetManager, appWidgetIds[i]);

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void setupNextRefresh(Context context, int id) {
        int type = ShotAppWidgetConfigure.getRefreshType(context, id);
        //time in seconds
        long time = -1;
        switch (type) {
            case ShotAppWidgetConfigure.REFRESH_MANUALLY:
                break;
            case ShotAppWidgetConfigure.REFRESH_5_MIN:
                time = 1 * 60;
                break;
            case ShotAppWidgetConfigure.REFRESH_15_MIN:
                time = 15 * 60;
                break;
            case ShotAppWidgetConfigure.REFRESH_30_MIN:
                time = 30 * 60;
                break;
            case ShotAppWidgetConfigure.REFRESH_1_HOUR:
                time = 60 * 60;
                break;
            case ShotAppWidgetConfigure.REFRESH_DAILY:
                time = 60 * 60 * 24;
                break;
        }

        if (time == -1) {
            return;
        }

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent refreshIntent = new Intent(context, ShotAppWidgetProvider.class);
        refreshIntent.setAction(ShotAppWidgetProvider.REFRESH_ACTION);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        refreshIntent.setData(Uri.parse(refreshIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time * 1000, alarmIntent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        if (appWidgetIds == null) {
            return;
        }
        for (int i = 0; i < appWidgetIds.length; i++) {
            ShotAppWidgetConfigure.cancelRefreshAlarm(context, appWidgetIds[i]);
        }

    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }
}
