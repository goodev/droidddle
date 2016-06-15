package org.goodev.droidddle.notif;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.goodev.droidddle.BuildConfig;
import org.goodev.droidddle.MainActivity;
import org.goodev.droidddle.R;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.wear.RetryException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Created by ADMIN on 2015/5/21.
 */
public class FollowingCheckService extends IntentService {

    public static final String ACTION_CHECKING = "org.goodev.droidddle.action.CHECK_FOLLOWING";

    public static final String ACTION_START = "org.goodev.droidddle.action.CHECK_INIT";

    private static final String TAG = "FollowingCheckService";

    private static final String PREF_RETRY_ATTEMPT = "retry_attempt";

    public static final String PREF_LAST_SHOT_ID = "last_shot_id";

    private static final int COLOR = Color.parseColor("#E91E63");

    private String mName;

    public FollowingCheckService() {
        super("FollowingCheckService");
        mName = "FollowingCheckService";
    }

    public static Intent getServiceIntent(Context context) {
        return new Intent(ACTION_START)
                .setComponent(new ComponentName(context, FollowingCheckService.class));
    }

    private PendingIntent getHandleNextUpdatePendingIntent(Context context) {
        return PendingIntent.getService(context, 0, new Intent(ACTION_CHECKING)
                        .setComponent(new ComponentName(context, FollowingCheckService.class)),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected final void scheduleNextUpdate() {
        long time = Pref.getFollowingCheckTime(this);
        if (time == 0) {
            //scheduleUpdate(System.currentTimeMillis() + INITIAL_RETRY_DELAY_MILLIS * 10);
            return;
        }
        scheduleUpdate(System.currentTimeMillis() + time);
    }

    protected final void scheduleUpdate(long scheduledUpdateTimeMillis) {
        setUpdateAlarm(scheduledUpdateTimeMillis);
    }

    private void setUpdateAlarm(long nextTimeMillis) {
        if (nextTimeMillis < System.currentTimeMillis()) {
            Log.w(TAG, "Refusing to schedule next artwork in the past, id=");
            return;
        }
        clearUpdateAlarm();

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC, nextTimeMillis, getHandleNextUpdatePendingIntent(this));
        Log.i(TAG, "Scheduling next artwork (source " + ".." + ") at " + new Date(nextTimeMillis));
    }

    private void clearUpdateAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(getHandleNextUpdatePendingIntent(this));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long time = Pref.getFollowingCheckTime(this);
        if (time == 0) {
            // disable the check
            return;
        }
        if (intent == null) {
            scheduleNextUpdate();
            return;
        }
        String action = intent.getAction();
        if (ACTION_CHECKING.equals(action)) {
            onUpdate();
        } else {
            scheduleNextUpdate();
        }
    }

    private boolean hasAuthToken() {
        String token = OAuthUtils.getAccessToken(getApplicationContext());
        return !TextUtils.isEmpty(token);
    }

    protected final SharedPreferences getSharedPreferences() {
        return getSharedPreferences("following", 0);
    }

    public final static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("following", 0);
    }

    private static final int INITIAL_RETRY_DELAY_MILLIS = 10 * 60 * 1000;// retry after 10 mins
    private static final int FETCH_WAKELOCK_TIMEOUT_MILLIS = 60 * 1000;

    protected void onUpdate() {
        if (!hasAuthToken()) {
            scheduleNextUpdate();
        }

        PowerManager pwm = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock lock = pwm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, mName);
        lock.acquire(FETCH_WAKELOCK_TIMEOUT_MILLIS);
        SharedPreferences sp = getSharedPreferences();
        try {
            NetworkInfo ni = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
            if (ni == null || !ni.isConnected()) {
                Log.d(TAG, "No network connection; not attempting to fetch update, id=" + mName);
                throw new RetryException();
            }
            // In anticipation of update success, reset update attempt
            // Any alarms will be cleared before onUpdate is called
            sp.edit().remove(PREF_RETRY_ATTEMPT).apply();
            // Attempt an update
            onTryUpdate();
        } catch (RetryException e) {
            Log.w(TAG, "Error fetching, scheduling retry, id=" + mName);
            // Schedule retry with exponential backoff, starting with INITIAL_RETRY... seconds later
            int retryAttempt = sp.getInt(PREF_RETRY_ATTEMPT, 0);
            scheduleUpdate(
                    System.currentTimeMillis() + (INITIAL_RETRY_DELAY_MILLIS << retryAttempt));
            sp.edit().putInt(PREF_RETRY_ATTEMPT, retryAttempt + 1).apply();
        } catch (Exception e) {
            // 401 exception
        } finally {
            if (lock.isHeld()) {
                lock.release();
            }
        }
    }

    protected void onTryUpdate() throws RetryException {
        Long lastId = getSharedPreferences().getLong(PREF_LAST_SHOT_ID, -1);
        List<Shot> shots = ApiFactory.getMuzeiService(getApplicationContext())
                .getUserFollowingShotsSync(1);
        if (shots.isEmpty()) {
            Log.w(TAG, "No shots returned from API.");
            scheduleNextUpdate();
            return;
        }
        ArrayList<Shot> newShots = new ArrayList<Shot>();
        if (BuildConfig.DEBUG) {
            lastId = null; // TODO just for test
        }
        for (Shot s : shots) {
            if (s.id.equals(lastId)) {
                break;
            }
            newShots.add(s);
        }

        notifyNewShots(newShots);

        scheduleNextUpdate();
    }

    //throw Notification to notify
    private void notifyNewShots(ArrayList<Shot> shots) {
        if (shots.isEmpty()) {
            return;
        }

        getSharedPreferences().edit().putLong(PREF_LAST_SHOT_ID, shots.get(0).id).apply();

        int size = shots.size();
        // if only one shot, using BigPictureStyle
        // TODO test one notification
        boolean test = false && BuildConfig.DEBUG && new Random().nextBoolean();
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        boolean sound = audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
        if (size == 1 || test) {
            postShot(shots.get(0), sound);
        } else {
            //if have more shot, using InboxStyle and wearable pages...
            postShots(shots, sound);

        }

    }

    //@formatter:off
    private void postShots(ArrayList<Shot> shots, boolean sound) {
        int notificationId = 1001;
        // Build intent for notification content
        Intent viewIntent = new Intent(this, MainActivity.class);
        viewIntent.setAction(UiUtils.ACTION_OPEN_FOLLOWING);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Shot shot = shots.get(0);
        long when = System.currentTimeMillis();//shot.createdAt == null ? System.currentTimeMillis() : shot.createdAt.getTime();

        final int size = shots.size();
        final String title = getResources().getString(R.string.notification_new_shots_title,
                size > 9 ? size + "+" : String.valueOf(size));
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        //                .setSummaryText("+3 more");
        HashSet<String> users = new HashSet<String>();
        for (int i = 0; i < size && i < 5; i++) {
            Shot s = shots.get(i);
            postShot(s, false);
            CharSequence inbox = getInlineText(s);
            inboxStyle.addLine(inbox);
            users.add(s.user.name);
        }
        StringBuilder sb = new StringBuilder("By ");
        int length = users.size();
        int index = 1;
        for (String user : users) {
            if (index > 2) {
                sb.append(" and others");
                break;
            }
            if (index == 2) {
                sb.append(" , ");
            }
            sb.append(user);
            index++;
        }
        String byUsers = sb.toString();
        inboxStyle.setSummaryText(byUsers);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(byUsers)
                .setSmallIcon(R.drawable.ic_stat_shot)
                .setTicker(title)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setContentIntent(viewPendingIntent)
                .setGroup(GROUP_KEY)
                .setShowWhen(true)
                .setWhen(when)
                .setGroupSummary(true)
                .setColor(COLOR)
                .setLights(COLOR, 400, 5600)
                .setStyle(inboxStyle);
        if (!isMuteTime() && sound) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        }
        notify(builder.build(), "tag" + notificationId, notificationId);

    }

    //@formatter:on
    private void postShot(Shot shot, boolean sound) {
        int notificationId = shot.id.intValue();
        notify(getNotification(shot, sound), "tag" + notificationId, notificationId);
    }

    //@formatter:off
    private Spannable getInlineText(Shot shot) {
        final String name = shot.user.name;
        Spannable sb = new SpannableString(name + ": " + shot.title);
        sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //        sb.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 14, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }
    //@formatter:on

    private static final String GROUP_KEY = "new_shots_group";

    //@formatter:on
    private Notification getNotification(Shot shot, boolean sound) {
        int notificationId = shot.id.intValue();
        // Build intent for notification content
        Intent viewIntent = new Intent(this, MainActivity.class);
        viewIntent.setAction(UiUtils.ACTION_OPEN_SHOT);
        viewIntent.putExtra(UiUtils.ARG_ID, shot.id.longValue());
        viewIntent.putExtra(UiUtils.ARG_SHOT, shot);
        PendingIntent viewPendingIntent = PendingIntent
                .getActivity(this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String ticker = shot.title + " by " + shot.user.name;
        long when = System.currentTimeMillis();//shot.createdAt == null ? System.currentTimeMillis() : shot.createdAt.getTime();
        Bitmap shotImage = getShotImage(shot);
        Bitmap avatar = getUserAvatar(shot);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_shot).setContentTitle(shot.title)
                .setContentText(getDescription(shot)).setAutoCancel(true).setCategory(
                        NotificationCompat.CATEGORY_SOCIAL)
                //.setContentInfo(shot.user.name)
                .setContentIntent(viewPendingIntent).setGroup(GROUP_KEY).setShowWhen(true)
                .setWhen(when).setTicker(ticker).setColor(COLOR).setLights(COLOR, 400, 5600)
                .setSubText(shot.user.name);
        if (sound && !isMuteTime()) {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        }
        if (avatar != null) {
            notificationBuilder.setLargeIcon(avatar);
        }

        if (shotImage != null) {
            NotificationCompat.BigPictureStyle pictureStyle = new NotificationCompat.BigPictureStyle();
            pictureStyle.bigPicture(shotImage).setBigContentTitle(shot.title);
            //.setSummaryText(getDescriptioin(shot));
            notificationBuilder.setStyle(pictureStyle);
        }
        return notificationBuilder.build();
    }

    //@formatter:off
    private Spanned getDescription(Shot shot) {
        if (TextUtils.isEmpty(shot.description)) {
            return null;
        }
        return Html.fromHtml(shot.description);
    }

    // if is night , do not vibrate and sound
    private boolean isMuteTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        Date date1 = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        Date date2 = calendar.getTime();
        Date date = new Date();
        return date.after(date1) && date.before(date2);
    }

    private Bitmap getShotImage(Shot shot) {
        try {
            String urlToDownload = shot.images.teaser;
            return getImage(urlToDownload);
        } catch (Exception e) {
        }
        return null;
    }

    private Bitmap getUserAvatar(Shot shot) {
        if (shot.user != null && shot.user.avatarUrl != null) {
            return getImage(shot.user.avatarUrl);
        }
        return null;
    }

    OkHttpClient mOkHttpClient;

    private Bitmap getImage(String url) {
        try {
            if (mOkHttpClient == null) {
                mOkHttpClient = ApiFactory.getOkHttpClient(this);
            }
            Request request = new Request.Builder().url(url).build();
            Response response = mOkHttpClient.newCall(request).execute();

            if (response.code() == 200) {
                InputStream inputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                } catch (IOException ignore) {
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOkHttpClient = null;
    }

    private void notify(Notification noti, String tag, int id) {
        NotificationManagerCompat.from(this).notify(tag, id, noti);
    }
}
