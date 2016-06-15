package org.goodev.droidddle.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;

import org.goodev.droidddle.R;
import org.goodev.droidddle.UpActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by goodev on 2015/1/22.
 */
public class ShotAppWidgetConfigure extends UpActivity implements AdapterView.OnItemClickListener {
    public static final String WIDGET_PREF_NAME = "widget_pref.xml";
    public static final String KEY_REFRESH_TIME_PREFIX = "shots_refresh_";
    public static final int REFRESH_MANUALLY = 0;
    public static final int REFRESH_5_MIN = 1;

    //    @Override
    //    public boolean onCreateOptionsMenu(Menu menu) {
    //        // Inflate the menu; this adds items to the action bar if it is present.
    //        getMenuInflater().inflate(R.menu.menu_widget, menu);
    //        return true;
    //    }
    public static final int REFRESH_15_MIN = 2;
    public static final int REFRESH_30_MIN = 3;
    public static final int REFRESH_1_HOUR = 4;
    public static final int REFRESH_DAILY = 5;
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    @InjectView(R.id.list_view)
    ListView mListView;

    public static SharedPreferences getPref(Context context) {
        return context.getSharedPreferences(WIDGET_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int getRefreshType(Context context, int id) {
        SharedPreferences preferences = getPref(context);
        return preferences.getInt(KEY_REFRESH_TIME_PREFIX + id, 0);
    }

    public static PendingIntent getRefreshIntent(Context context, int id) {
        Intent refreshIntent = new Intent(context, ShotAppWidgetProvider.class);
        refreshIntent.setAction(ShotAppWidgetProvider.REFRESH_ACTION);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        refreshIntent.setData(Uri.parse(refreshIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return alarmIntent;
    }

    public static void cancelRefreshAlarm(Context context, int id) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = getRefreshIntent(context, id);
        alarmMgr.cancel(alarmIntent);
    }

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_shot_app_widget_config);
        ButterKnife.inject(this);
        String[] entries = getResources().getStringArray(R.array.refresh_entries);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, entries);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    void update() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.shot_appwidget);
        appWidgetManager.updateAppWidget(mAppWidgetId, views);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            update();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences preferences = getPref(this);
        preferences.edit().putInt(KEY_REFRESH_TIME_PREFIX + mAppWidgetId, position).commit();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ShotAppWidgetProvider.setupWidget(this, appWidgetManager, mAppWidgetId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);

        //        final Intent refreshIntent = new Intent(this, ShotAppWidgetProvider.class);
        //        refreshIntent.setAction(ShotAppWidgetProvider.REFRESH_ACTION);
        //        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        //        sendBroadcast(refreshIntent);

        setupNextRefresh(this, mAppWidgetId);
        finish();
    }

    private void setupNextRefresh(Context context, int id) {
        int type = ShotAppWidgetConfigure.getRefreshType(context, id);
        //time in seconds
        long time = -1;
        switch (type) {
            case ShotAppWidgetConfigure.REFRESH_MANUALLY:
                break;
            case ShotAppWidgetConfigure.REFRESH_5_MIN:
                time = 5 * 60;
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
        PendingIntent alarmIntent = getRefreshIntent(context, id);
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 2000, time * 1000, alarmIntent);
    }
}
