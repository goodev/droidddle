package org.goodev.droidddle.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.goodev.droidddle.App;

/**
 * Created by goodev on 2014/12/31.
 */
public class ShotPref {
    public static final String PREF_NAME = "shot_prefs";
    public static final String KEY_COMMENT = "key_comment_";

    public static SharedPreferences getPref() {
        return getPref(App.getContext());
    }

    public static SharedPreferences getPref(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveComment(long shotId, String text) {
        SharedPreferences pref = getPref();
        String key = KEY_COMMENT + shotId;
        pref.edit().putString(key, text).commit();
    }

    public static void removeComment(long shotId) {
        SharedPreferences pref = getPref();
        String key = KEY_COMMENT + shotId;
        pref.edit().remove(key).commit();
    }

    public static String getComment(long shotId) {
        SharedPreferences pref = getPref();
        String key = KEY_COMMENT + shotId;
        return pref.getString(key, null);
    }
}
