package org.goodev.droidddle.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import org.goodev.droidddle.App;
import org.goodev.droidddle.DribbbleLogin;
import org.goodev.droidddle.R;

import java.util.Locale;

/**
 * Created by goodev on 2014/12/16.
 */
public class OAuthUtils {
    public static final String LOGIN_CALLBACK = "phone-callback";
    public static final String CALLBACK_URI = "droidddle://" + LOGIN_CALLBACK;
    public static final String PREFERENCE_NAME = "auth";
    public static final String KEY_OAUTH_TOKEN = "auth_token";
    //14d7c62ce4cbfe763787e28ba6821ea2dc39f316337b83a1952139f64eae04fe
    //    private static final String CLIENT_ID = "bd483cfd1967a2afb3ef0057cdeeb347df16b67abc42e4491d104eb2202fdc65";
    //    private static final String CLIENT_SECRET = "b3e7f717e0ae2bb58e43dd6bdc8a150ae7b4fcf938dadd316b0095db42817b4b";
    private static final String CLIENT_ID = "mQ0ODNjZmQxOTY3YTJhZmIzZWYwMDU3Y2RlZWIzNDdkZjE2YjY3YWJjNDJlNDQ5MWQxMDRlYjIyMDJmZGM2NQ";
    //YmQ0ODNjZmQxOTY3YTJhZmIzZWYwMDU3Y2RlZWIzNDdkZjE2YjY3YWJjNDJlNDQ5MWQxMDRlYjIyMDJmZGM2NQ==
    //YjNlN2Y3MTdlMGFlMmJiNThlNDNkZDZiZGM4YTE1MGFlN2I0ZmNmOTM4ZGFkZDMxNmIwMDk1ZGI0MjgxN2I0Yg==
    private static final String CLIENT_SECRET = "jNlN2Y3MTdlMGFlMmJiNThlNDNkZDZiZGM4YTE1MGFlN2I0ZmNmOTM4ZGFkZDMxNmIwMDk1ZGI0MjgxN2I0Yg";
    public static final String ACCESS_TOKEN = "1bd103a9f293ae9ff346845866155091dea30e902aad1dbab4c9372ad661aa06";
    public static final int LOGIN_CODE = 121;
    private static String TOKEN;


    public static void clearOAuthCredential(Context context) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit().clear().commit();
        deleteAccessToken(context);
        App.clearApplicationData(context);
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences pref = Pref.getPref(context);
        String token = pref.getString(KEY_OAUTH_TOKEN, null);
        return !TextUtils.isEmpty(token);
    }

    public static String getLoginUrl() {
        return "https://dribbble.com/oauth/authorize?client_id="
                + OAuthUtils.getClientId()
                + "&redirect_uri=droidddle%3A%2F%2F" + LOGIN_CALLBACK
                + "&scope=public+write+comment+upload";
    }

    public static void startOauth(AppCompatActivity activity) {
        String userId = Pref.getOAuthUserIndex(activity);
        String id = getClientId();
        String secret = getClientSecret();

        Intent intent = new Intent(activity, DribbbleLogin.class);
        intent.setData(Uri.parse(getLoginUrl()));
        activity.startActivityForResult(intent, LOGIN_CODE);
        Toast.makeText(activity, R.string.action_login, Toast.LENGTH_LONG).show();
        //@f:off
    }

    public static String getClientId() {
        StringBuilder sb = new StringBuilder("y".toUpperCase(Locale.ENGLISH));
        sb.append(CLIENT_ID).append("==");
        return new String(Base64.decode(sb.toString(), Base64.DEFAULT));

    }

    public static String getClientSecret() {
        StringBuilder sb = new StringBuilder("y".toUpperCase(Locale.ENGLISH));
        sb.append(CLIENT_SECRET).append("==");
        return new String(Base64.decode(sb.toString(), Base64.DEFAULT));
    }

    public static void oauthCanceled(Context context) {
        Toast.makeText(context, R.string.oauth_canceled, Toast.LENGTH_LONG).show();
    }

    public static boolean haveToken(Context context) {
        return getAccessToken(context) != null;
    }


    public static void saveAccessToken(Context context, String accessToken) {
        TOKEN = accessToken;
        //SecurePreferences pref = new SecurePreferences(context);
        SharedPreferences pref = Pref.getPref(context);
        pref.edit().putString(KEY_OAUTH_TOKEN, accessToken).commit();
    }

    public static void deleteAccessToken(Context context) {
        TOKEN = null;
        //SecurePreferences pref = new SecurePreferences(context);
        SharedPreferences pref = Pref.getPref(context);
        pref.edit().remove(KEY_OAUTH_TOKEN).commit();
    }

    public static void removeTokenCacke() {
        TOKEN = null;
    }

    public static String getAccessToken(Context context) {
        if (TOKEN != null) {
            return TOKEN;
        }
        //        SecurePreferences pref = new SecurePreferences(context);
        SharedPreferences pref = Pref.getPref(context);
        TOKEN = pref.getString(KEY_OAUTH_TOKEN, null);
        if (TOKEN == null) {
            return ACCESS_TOKEN;
        }
        return TOKEN;
    }

    public static boolean isSelf(long id) {
        return id == Pref.getOAuthUserId(App.getContext());
    }
}
