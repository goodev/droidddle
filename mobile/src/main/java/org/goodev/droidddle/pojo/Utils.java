package org.goodev.droidddle.pojo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by yfcheng on 2015/4/14.
 */
public class Utils {
    public static final String DEFAULT_SHOT_IMAGE_URL = "https://d13yacurqjgara.cloudfront.net/users/115601/screenshots/1620513/cake800_1_1x.png";

    public static String getShotImageUrl(Shot shot) {
        Image images = shot.images;
        if (images == null) {
            return DEFAULT_SHOT_IMAGE_URL;
        }
        return TextUtils.isEmpty(images.hidpi) ? images.normal : images.hidpi;
    }

    public static String getShotViewIntent(Shot shot) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(shot.htmlUrl));
        return intent.toUri(Intent.URI_INTENT_SCHEME);
    }

    public static String getShotUserName(Shot shot) {
        if (shot.user != null) {
            return shot.user.name;
        }
        if (shot.team != null) {
            return shot.team.name;
        }
        return "";
    }

//    public static Shot shotFromBundle(Bundle bundle) {
//        Shot shot = new Shot();
//        shot.title = bundle.getString(Shot.KEY_TITLE);
//        shot.description = bundle.getString(Shot.KEY_DES);
//        shot.htmlUrl = bundle.getString(Shot.KEY_DETAILS_URI);
//        shot.id = bundle.getLong(Shot.KEY_TOKEN);
//        shot.user = new User();
//        shot.user.name = bundle.getString(Shot.KEY_BYLINE);
//
//        shot.images = new Image();
//        shot.images.hidpi = bundle.getString(Shot.KEY_IMAGE_URI);
//        return shot;
//    }
//
//    public static void saveShotToPref(Context context, Shot shot) {
//        SharedPreferences pref = getShotPref(context);
//        pref.edit().putString(Shot.KEY_BYLINE, Utils.getShotUserName(shot))
//                .putString(Shot.KEY_DES, shot.description)
//                .putString(Shot.KEY_DETAILS_URI, shot.htmlUrl)
//                .putString(Shot.KEY_TITLE, shot.title)
//                .putLong(Shot.KEY_TOKEN, shot.id)
//                .putString(Shot.KEY_IMAGE_URI, Utils.getShotImageUrl(shot))
//                .commit()
//        ;
//    }

    public static SharedPreferences getShotPref(Context context) {
        SharedPreferences pre = context.getSharedPreferences("currentshot", 0);
        return pre;
    }
}
