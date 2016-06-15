package org.goodev.droidddle.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;

import org.goodev.droidddle.App;
import org.goodev.droidddle.pojo.User;

import java.io.File;

/**
 * Created by goodev on 2014/12/22.
 */
public class Pref {

    public static final String USER_OAUTH_INDEX_PRE = "oauth_user_";
    public static final int USER_OAUTH_INDEX_DEFAULT = 1;

    public static final String PREF_NAME = "prefs";
    public static final String KEY_USER_OAUTH_INDEX = "key_oauth_user_index";
    public static final String KEY_USER_OAUTH_ID = "key_oauth_userid";
    public static final String KEY_FIRST_LIKE_WHY = "key_like_unlike_why";
    public static final int DEFAULT_THEME = 0;
    public static final String KEY_INIT_AD = "key_init_a";
    public static final String KEY_SHOW_AD = "key_show_a";
    public static final String KEY_DETAILS_AD = "key_details_a";
    public static final String KEY_SHOW_FULL_AD = "key_show_full_a";

    public static final String KEY_HOME_AD = "key_home_a";
    private static final String KEY_SHOW_TAG = "key_show_tag_tip";

    public static int getOrientation(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int value = 0;
        try {
            value = Integer.valueOf(pref.getString(Settings.ORIENTATION, "0"));
        } catch (NumberFormatException e) {
        }
        if (value == 0) {
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        } else if (value == 1) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    public static long getFollowingCheckTime(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        long value = 0;
        try {
            value = Long.valueOf(pref.getString(Settings.CHECK_TIME, "240"));
        } catch (NumberFormatException e) {
        }

        return value * 60 * 1000;
    }

    public static int getTheme(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return Integer.valueOf(pref.getString(Settings.THEME, String.valueOf(DEFAULT_THEME)));
        } catch (NumberFormatException e) {
            return DEFAULT_THEME;
        }
    }

    public static boolean isCleanerMode(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(Settings.CLEANER_MODE, true);
    }

    public static void setTheme(Context context, String theme) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(Settings.THEME, theme).commit();
    }

    public static boolean getSendByShot(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(Settings.SEND_BY_SHOT, true);
    }

    public static boolean getSendByComment(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(Settings.SEND_BY_COMMENT, true);
    }

    public static SharedPreferences getPref(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getOAuthUserIndex(Context context) {
        SharedPreferences preferences = getPref(context);
        int id = preferences.getInt(KEY_USER_OAUTH_INDEX, USER_OAUTH_INDEX_DEFAULT);

        return USER_OAUTH_INDEX_PRE + id;
    }

    public static void setOAuthUserIndex(Context context, int index) {
        SharedPreferences preferences = getPref(context);
        preferences.edit().putInt(KEY_USER_OAUTH_INDEX, index).commit();
    }

    public static long getOAuthUserId(Context context) {
        SharedPreferences preferences = getPref(context);
        return preferences.getLong(KEY_USER_OAUTH_ID, UiUtils.NO_ID);
    }

    public static void setOAuthUserId(Context context, long id) {
        if (context == null)
            context = App.getContext();
        SharedPreferences preferences = getPref(context);
        preferences.edit().putLong(KEY_USER_OAUTH_ID, id).commit();
    }

    public static void updateUser(Context context, User user) {
        SharedPreferences preferences = getPref(context);
    }

    private static final String KEY_DL_DIR = "key_dldir";

    public static String getDownloadDir(Context context, File dir) {
        SharedPreferences preferences = getPref(context);
        String path = preferences.getString(KEY_DL_DIR, null);
        if (path == null) {
            return new File(dir, "droidddle").getAbsolutePath();
        }
        return path;
    }

    public static void setDownloadDir(Context context, String dir) {
        SharedPreferences preferences = getPref(context);
        preferences.edit().putString(KEY_DL_DIR, dir).apply();
    }

    public static boolean isFirstLikeAndUnlike(Context context) {
        SharedPreferences preferences = getPref(context);
        return preferences.getBoolean(KEY_FIRST_LIKE_WHY, true);
    }

    public static void hideFirstLikeAndUnlike(Context context) {
        SharedPreferences preferences = getPref(context);
        preferences.edit().putBoolean(KEY_FIRST_LIKE_WHY, false).commit();
    }

    public static void updateInitAds(Context context, boolean init) {
        SharedPreferences preferences = getPref(context);
        preferences.edit()//
                .putBoolean(KEY_INIT_AD, init)//
                .commit();
    }

    public static void updateAds(Context context, boolean showAd, boolean showFullAdd) {
        SharedPreferences preferences = getPref(context);
        preferences.edit()//
                .putBoolean(KEY_SHOW_AD, showAd)//
                .putBoolean(KEY_SHOW_FULL_AD, showFullAdd)//
                .commit();
    }

    public static boolean isInitShowAd(Context context) {
        SharedPreferences preferences = getPref(context);
        return preferences.getBoolean(KEY_INIT_AD, false);
    }

    public static boolean isShowAd(Context context) {
        SharedPreferences preferences = getPref(context);
        return preferences.getBoolean(KEY_SHOW_AD, false);
    }

    public static boolean isShowFullAd(Context context) {
        SharedPreferences preferences = getPref(context);
        return preferences.getBoolean(KEY_SHOW_FULL_AD, false);
    }

    public static boolean isShowDetailsAd(Context context) {
        SharedPreferences preferences = getPref(context);
        return preferences.getBoolean(KEY_DETAILS_AD, false);
    }

    public static void updateShowDetailsAd(Context context, boolean showDetailsAdd) {
        SharedPreferences preferences = getPref(context);
        preferences.edit()//
                .putBoolean(KEY_DETAILS_AD, showDetailsAdd)//
                .commit();
    }

    public static void saveShowAdsStatus(Context context, boolean isChecked) {
        SharedPreferences preferences = getPref(context);
        preferences.edit()//
                .putBoolean(KEY_HOME_AD, isChecked)//
                .commit();
    }

    public static boolean isShowHomeAds(Context context) {
        SharedPreferences preferences = getPref(context);
        return preferences.getBoolean(KEY_HOME_AD, true);
    }

    public static boolean needShowTagTips(Context context) {
        SharedPreferences preferences = getPref(context);
        return preferences.getBoolean(KEY_SHOW_TAG, true);
    }

    public static void setShowTagStatus(Context context, boolean show) {
        SharedPreferences preferences = getPref(context);
        preferences.edit()//
                .putBoolean(KEY_SHOW_TAG, show)//
                .commit();
    }
}
